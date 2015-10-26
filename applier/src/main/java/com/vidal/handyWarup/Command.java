package com.vidal.handyWarup;

import java.nio.file.Path;
import java.util.function.BiConsumer;

public interface Command extends BiConsumer<Path, Path> {
}
