package com.relationalai;

import org.apache.arrow.memory.RootAllocator;

public class ArrowUtils implements AutoCloseable {
    private static RootAllocator allocator;

    public static RootAllocator getOrCreateRootAllocator() {
        if (allocator == null) {
            allocator = new RootAllocator(Long.MAX_VALUE);
        }

        return allocator;
    }

    @Override
    public void close() throws Exception {
        allocator.close();
    }
}
