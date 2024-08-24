const sdl = require("@kmamal/sdl");
const {createCanvas} = require("canvas");

const SCREEN_WIDTH = 640;
const SCREEN_HEIGHT = 480;

const window = sdl.video.createWindow({width: SCREEN_WIDTH, height: SCREEN_HEIGHT, title: "SPL Window"});
const canvas = createCanvas(SCREEN_WIDTH, SCREEN_HEIGHT);
const canvasContext = canvas.getContext("2d");

canvasContext.fillStyle = "white";
canvasContext.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
render();

function intToColor(n) {
    n >>>= 0;
    const b = n & 0xff,
        g = (n & 0xff00) >>> 8,
        r = (n & 0xff0000) >>> 16;
    return `rgb(${r}, ${g}, ${b})`;
}

function render() {
    const buffer = canvas.toBuffer("raw");
    window.render(SCREEN_WIDTH, SCREEN_HEIGHT, SCREEN_WIDTH * 4, "bgra32", buffer);
}

async function _clearAll(color) {
    canvasContext.fillStyle = intToColor(color);
    canvasContext.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
    render();
}

async function _setPixel(x, y, color) {
    canvasContext.fillStyle = intToColor(color);
    canvasContext.fillRect(x, y, 1, 1);
    render();
}

async function _drawLine(x1, y1, x2, y2, color) {
    canvasContext.strokeStyle = intToColor(color);
    canvasContext.beginPath();
    canvasContext.moveTo(x1, y1);
    canvasContext.lineTo(x2, y2);
    canvasContext.stroke();
    render();
}

async function _drawCircle(x0, y0, radius, color) {
    canvasContext.strokeStyle = intToColor(color);
    canvasContext.beginPath();
    canvasContext.arc(x0, y0, radius, 0, 2 * Math.PI);
    canvasContext.stroke();
    render();
}
