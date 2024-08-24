import fs from "fs/promises";

const wasmMemory = new WebAssembly.Memory({
    initial: 1,
    maximum: 100,
    shared: false,
});

// Not 8implemented because unused
const env = {
    printi(value) { },
    printc(value) { },
    readi(address) { },
    readc(address) { },
    exit() { },
    time(address) { },
    clearAll(color) { },
    setPixel(x, y, color) { },
    drawLine(x1, y1, x2, y2, color) { },
    drawCircle(x0, y0, radius, color) { }
};

const wasmBuffer = await fs.readFile(process.argv[2]);
const wasmModule = await WebAssembly.instantiate(wasmBuffer, { env: { ...env, memory: wasmMemory } });

const { main } = wasmModule.instance.exports;

main();