# Architecture-dependent library variables
COMPILE := gcc
DELETE := @rm
MKDIR := @mkdir
COPY := @cp
MOVE := @mv
PRINT := @echo
JDK_HOME := $(shell if [ "`uname`" = "Darwin" ]; then echo "`/usr/libexec/java_home`"; else echo "$$JDK_HOME"; fi)
INCLUDES := -I"$(JDK_HOME)/include" -I"$(JDK_HOME)/include/linux" -I"$(JDK_HOME)/include/darwin"
CFLAGS := -fPIC -Os -flto -static-libgcc -U_FORTIFY_SOURCE -D_FORTIFY_SOURCE=0
LDFLAGS := -shared
BUILD_DIR := ../native
LIBRARY_NAME_POSIX := libhelloJNI.so
LIBRARY_NAME_APPLE := libhelloJNI.jnilib

# Define phony and suffix rules
.PHONY: all clean linux osx
.SUFFIXES:
.SUFFIXES: .cpp .c .o .class .java .h

all:
	$(PRINT) You must specify either linux or osx!

clean:
	$(DELETE) -rf "$(BUILD_DIR)"

linux: $(BUILD_DIR) $(BUILD_DIR)/$(LIBRARY_NAME_POSIX)
osx: $(BUILD_DIR) $(BUILD_DIR)/$(LIBRARY_NAME_APPLE)

$(BUILD_DIR):
	$(MKDIR) -p $@

$(BUILD_DIR)/$(LIBRARY_NAME_POSIX): hello.c
	$(COMPILE) $(INCLUDES) $(CFLAG) $(LDFLAGS) $^ -o $@

$(BUILD_DIR)/$(LIBRARY_NAME_APPLE): hello.c
	$(COMPILE) $(INCLUDES) $(CFLAG) $(LDFLAGS) $^ -o $@