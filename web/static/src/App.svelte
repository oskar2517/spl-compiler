<script>
    import {
        BlobReader,
        BlobWriter,
        TextWriter,
        ZipReader,
    } from "@zip.js/zip.js";
    import { onMount } from "svelte";
    import Console from "./lib/Console.svelte";
    import Display from "./lib/Display.svelte";
    import FileInput from "./lib/FileInput.svelte";
    import { writeInSharedMemory, unlock } from "./lib/wasm";

    const JS_EXIT = "exit js execution";

    let mode = "wasm";

    let wasmCanvas;
    let jsCanvas;
    let jsCanvasContext;
    let consoleInput;
    let startTime;

    const jsContext = {
        async printi(value) {
            outputText += value;
        },
        async printc(value) {
            outputText += String.fromCharCode(value);
        },
        async readi(address) {
            const s = await readInt();

            address.value = s;
        },
        async readc(address) {
            const s = await readChar();

            address.value = s;
        },
        async exit() {
            throw new Error(JS_EXIT);
        },
        async time(address) {
            address.value = Math.floor(Date.now() / 1000) - startTime;
        },
        async clearAll(color) {
            jsCanvasContext.fillStyle = intToColor(color);
            jsCanvasContext.fillRect(0, 0, 640, 480);
        },
        async setPixel(x, y, color) {
            jsCanvasContext.fillStyle = intToColor(color);
            jsCanvasContext.fillRect(x, y, 1, 1);
        },
        async drawLine(x1, y1, x2, y2, color) {
            jsCanvasContext.strokeStyle = intToColor(color);
            jsCanvasContext.beginPath();
            jsCanvasContext.moveTo(x1, y1);
            jsCanvasContext.lineTo(x2, y2);
            jsCanvasContext.stroke();
        },
        async drawCircle(x0, y0, radius, color) {
            jsCanvasContext.strokeStyle = intToColor(color);
            jsCanvasContext.beginPath();
            jsCanvasContext.arc(x0, y0, radius, 0, 2 * Math.PI);
            jsCanvasContext.stroke();
        },
    };

    // Prevent removal of unused code
    // @ts-ignore
    window.jsContext = jsContext;

    onMount(() => {
        const offscreenCanvas = wasmCanvas.transferControlToOffscreen();

        wasmWorker.postMessage(
            {
                type: "init",
                sharedArrayBuffer,
                threadLock,
                offscreenCanvas,
            },
            [offscreenCanvas],
        );

        jsCanvasContext = jsCanvas.getContext("2d");
    });

    let outputText = "";
    let inputEnabled = false;

    const wasmWorker = new Worker("worker.js");
    const sharedArrayBuffer = new SharedArrayBuffer(4);
    const threadLock = new Int32Array(sharedArrayBuffer);
    const wasmMemory = new WebAssembly.Memory({
        initial: 1,
        maximum: 100,
        shared: true,
    });

    // TODO: duplicated
    function intToColor(n) {
        n >>>= 0;
        const b = n & 0xff,
            g = (n & 0xff00) >>> 8,
            r = (n & 0xff0000) >>> 16;
        return `rgb(${r}, ${g}, ${b})`;
    }

    function handleArrayOOB() {
        outputText += "array access out of bounds";
    }

    function executeFile(file, fileName) {
        const reader = new FileReader();

        reader.addEventListener("load", async () => {
            // @ts-ignore
            const arrayBuffer = new Uint8Array(reader.result);
            outputText = "";
            startTime = Math.floor(Date.now() / 1000);

            if (fileName.endsWith(".wasm")) {
                mode = "wasm";
                await wasmWorker.postMessage({
                    type: "execute",
                    bytes: arrayBuffer,
                    memory: wasmMemory,
                });
            } else if (fileName.endsWith(".js")) {
                const decoder = new TextDecoder("utf-8");

                mode = "js";
                jsCanvasContext.reset();
                eval(`${decoder.decode(arrayBuffer)} _main().catch(err => { if (err.message !== "${JS_EXIT}") handleArrayOOB(); });`);
        
            } else {
                throw `Unexpected file ${fileName}`;
            }
        });

        reader.readAsArrayBuffer(file);
    }

    async function handleReadFile(e) {
        const file = e.detail.file;

        if (file.name.endsWith(".zip")) {
            const blobReader = new BlobReader(file);
            const zipReader = new ZipReader(blobReader);
            const entries = await zipReader.getEntries();

            const manifestFile = await entries
                .find((e) => e.filename === "manifest.json")
                ?.getData(new TextWriter());

            if (!manifestFile) {
                throw "Manifest file missing";
            }

            const manifest = JSON.parse(manifestFile);
            const programs = manifest.files.entries();

            const interval = setInterval(async () => {
                const next = programs.next();

                if (next.done) {
                    clearInterval(interval);
                    return;
                }

                const filename = next.value[1];
                const program = await entries
                    .find((e) => e.filename === filename)
                    ?.getData(new BlobWriter());

                executeFile(program, filename);
            }, manifest.delay);
        } else if (file.name.endsWith(".wasm") || file.name.endsWith(".js")) {
            executeFile(file, file.name);
        } else {
            throw `Unexpected file ${file.name}`;
        }
    }

    function readChar() {
        return new Promise((resolve, reject) => {
            inputEnabled = true;
            setTimeout(() => {
                consoleInput.focus();
            }, 100);
            consoleInput.oninput = (e) => {
                const value = consoleInput.value;
                inputEnabled = false;
                consoleInput.value = "";
                resolve(value.charCodeAt(0));
            };
            consoleInput.onkeyup = (e) => {
                if (e.key === "Enter") {
                    resolve("\n".charCodeAt(0));
                }
            }
        });
    }

    function readInt() {
        return new Promise((resolve, reject) => {
            inputEnabled = true;
            setTimeout(() => {
                consoleInput.focus();
            }, 100);
            consoleInput.onkeyup = (e) => {
                if (e.key === "Enter") {
                    const value = consoleInput.value;
                    inputEnabled = false;
                    consoleInput.value = "";
                    resolve(parseInt(value));
                }
            };
        });
    }

    wasmWorker.addEventListener("message", async (e) => {
        switch (e.data.type) {
            case "arrayAccessOutOfBounds": {
                handleArrayOOB();
                break;
            }
            case "print": {
                outputText += e.data.value;
                break;
            }
            case "readi": {
                const s = await readInt();

                writeInSharedMemory(wasmMemory, e.data.address, s);
                unlock(threadLock);
                break;
            }
            case "time": {
                writeInSharedMemory(wasmMemory, e.data.address, Math.floor(Date.now() / 1000) - startTime);
                unlock(threadLock);
                break;
            }
            case "readc": {
                const s = await readChar();

                writeInSharedMemory(
                    wasmMemory,
                    e.data.address,
                    s,
                );
                unlock(threadLock);
                break;
            }
        }
    });
</script>

<main>
    <h1>SPL to WASM</h1>

    <div class="row">
        <FileInput on:readFile={handleReadFile} />
    </div>

    <div class="row">
        <Display>
            <canvas
                class="display-canvas"
                class:visible={mode === "wasm"}
                height="480"
                width="640"
                bind:this={wasmCanvas}
            />
            <canvas
                class="display-canvas"
                class:visible={mode === "js"}
                height="480"
                width="640"
                bind:this={jsCanvas}
            />
        </Display>
        <Console {inputEnabled} text={outputText} bind:consoleInput />
    </div>
</main>

<style>
    main {
        display: flex;
        align-items: center;
        flex-direction: column;
    }

    h1 {
        font-family: sans-serif;
        margin: 20px;
    }

    .display-canvas {
        display: none;
    }

    .display-canvas.visible {
        display: block;
    }

    .row {
        display: flex;
        column-gap: 20px;
        margin-bottom: 20px;
    }
</style>
