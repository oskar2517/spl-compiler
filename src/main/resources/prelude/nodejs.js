const fs = require("fs");
const readline = require("readline");

const startTime = Math.floor(Date.now() / 1000);

function checkArrayIndex(index, arraySize) {
    if (index < 0 || index >= arraySize) {
        throw new Error("array access out of bounds")
    }
    return index
}

async function _time(time) {
    time.value = Math.floor(Date.now() / 1000) - startTime;
}

async function _printi(i) {
    process.stdout.write(i.toString());
}

async function _printc(c) {
    process.stdout.write(String.fromCharCode(c));
}

async function _readc(target) {
    return new Promise(async (resolve, reject) => {
        readline.emitKeypressEvents(process.stdin);
        process.stdin.setRawMode(true);
        const buffer = Buffer.alloc(1);
        fs.read(process.stdin.fd, buffer, 0, 1, null, (err, bytesRead, buffer) => {
            if (err) {
                throw err;
            }
            process.stdin.setRawMode(false);
            const char = buffer.toString("utf8").charCodeAt(0);

            target.value = char;
            resolve();
        });
    });
}

async function _readi(target) {
    return new Promise(async (resolve, reject) => {
        const buffer = Buffer.alloc(1024);
        fs.read(process.stdin.fd, buffer, 0, buffer.length, null, (err, bytesRead, buffer) => {
            if (err) {
                throw err;
            }

            const number = parseInt(buffer.toString("utf8", 0, bytesRead).trim());

            if (isNaN(number)) {
                target.value = 0;
            } else {
                target.value = number;
            }

            resolve();
        });
    });
}

async function _exit() {
    process.exit();
}
