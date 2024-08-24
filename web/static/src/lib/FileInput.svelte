<script>
    import { createEventDispatcher } from "svelte";

    const dispatch = createEventDispatcher();

    function handleChange(e) {
        const files = e.target.files;

        if (files.length === 0) {
            return;
        }

        dispatch("readFile", { file: files[0] });
    }
</script>

<input type="file" on:input={handleChange} />
