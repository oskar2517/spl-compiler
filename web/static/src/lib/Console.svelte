<script>
    import { afterUpdate } from "svelte";

    export let text;
    export let inputEnabled;
    export let consoleInput;

    let output;

    afterUpdate(() => {
        if (text.endsWith("\n")) {
            output.scrollTop = output.scrollHeight;
        }
    });
</script>

<div class="console">
    <textarea
        bind:this={output}
        readonly
        class="output"
        value={text}
        placeholder="No output"
    />

    <input
        type="text"
        placeholder=">"
        bind:this={consoleInput}
        disabled={!inputEnabled}
        class="input"
    />
</div>

<style>
    .console {
        display: flex;
        flex-direction: column;
        row-gap: 10px;
    }

    .output {
        width: 800px;
        min-height: 400px;
        height: 438px;
        resize: vertical;
        font-family: monospace;
        font-size: 16px;
        padding: 5px;
        border: solid 1px black;
    }

    .input {
        width: 800px;
        font-family: sans-serif;
        font-size: 16px;
        padding: 5px;
        border: solid 1px black;
        font-family: monospace;
    }
</style>
