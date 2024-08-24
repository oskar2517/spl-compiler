export function writeInSharedMemory(memory, address, value) {
    const int32Array = new Int32Array(memory.buffer);
    int32Array[address / 4] = value;
}

export function unlock(threadLock) {
    Atomics.store(threadLock, 0, 0);
    Atomics.notify(threadLock, 0);
}
