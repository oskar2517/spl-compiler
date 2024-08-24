const WASM_EXIT = "exit wasm execution";
let sharedArrayBuffer;
let threadLock;
let canvasContext;

function lock() {
    Atomics.store(threadLock, 0, 1);
    Atomics.notify(threadLock, 0);
    Atomics.wait(new Int32Array(sharedArrayBuffer), 0, 1);
}

function intToColor(n) {
    n >>>= 0;
    const b = n & 0xFF,
        g = (n & 0xFF00) >>> 8,
        r = (n & 0xFF0000) >>> 16;
    return `rgb(${r}, ${g}, ${b})`;
}

const env = {
    printi(value) {
        postMessage({
            type: "print",
            value
        });
    },
    printc(value) {
        postMessage({
            type: "print",
            value: String.fromCharCode(value)
        });
    },
    readi(address) {
        postMessage({
            type: "readi",
            address
        });

        lock();
    },
    readc(address) {
        postMessage({
            type: "readc",
            address
        });

        lock();
    },
    exit() {
        throw new Error(WASM_EXIT);
    },
    async time(address) {
        await postMessage({
            type: "time",
            address
        });

        lock();
    },
    clearAll(color) {
        canvasContext.fillStyle = intToColor(color);
        canvasContext.fillRect(0, 0, 640, 480);
    },
    setPixel(x, y, color) {
        canvasContext.fillStyle = intToColor(color);
        canvasContext.fillRect(x, y, 1, 1);
    },
    drawLine(x1, y1, x2, y2, color) {
        canvasContext.strokeStyle = intToColor(color);
        canvasContext.beginPath();
        canvasContext.moveTo(x1, y1);
        canvasContext.lineTo(x2, y2);
        canvasContext.stroke();
    },
    drawCircle(x0, y0, radius, color) {
        canvasContext.strokeStyle = intToColor(color);
        canvasContext.beginPath();
        canvasContext.arc(
            x0,
            y0,
            radius,
            0,
            2 * Math.PI,
        );
        canvasContext.stroke();
    }
};


onmessage = async (e) => {
    switch (e.data.type) {
        case "init": {
            sharedArrayBuffer = e.data.sharedArrayBuffer;
            threadLock = e.data.threadLock;
            canvasContext = e.data.offscreenCanvas.getContext("2d");

            break;
        }
        case "execute": {
            try {
                const wasm = await WebAssembly.instantiate(e.data.bytes, { env: { ...env, memory: e.data.memory } });
                const exports = wasm.instance.exports;
    
                canvasContext.reset();
                exports.main();
            } catch (err) {
                if (err.message !== WASM_EXIT) {
                    await postMessage({
                        type: "arrayAccessOutOfBounds",
                    });
                }
            }

            break;
        }
    }
};