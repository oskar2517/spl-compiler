const express = require("express");

const app = express();

app.listen(3000, () => {
    console.log("Server running on port 3000");
});

app.use("", express.static("static/dist", {
    setHeaders: function (res, path) {
        res.set("Cross-Origin-Opener-Policy", "same-origin");
        res.set("Cross-Origin-Embedder-Policy", "require-corp");
    }
}));