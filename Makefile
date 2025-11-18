JC = javac -source 11 -g

# Windows uses ; as a path separator while Linux and Mac use :
SEP = :
ifeq ($(OS),Windows_NT)
	SEP = ;
endif

OUT = java/out
DEPS = java/deps

JAVA_SRC = $(shell find java/hw/*/src -name "*.java")
JAVA_TST = $(shell find java/hw/*/tst -name "*.java")

.PHONY: all clean run test

all: $(OUT)/src/ $(OUT)/tst/

$(OUT)/src/: $(JAVA_SRC)
	@ echo "[javac] $@"
	@ mkdir -p $@
	@ touch $@
	$(JC) -d $@ -cp "$(DEPS)/*" $(JAVA_SRC)

$(OUT)/tst/: $(JAVA_TST) $(OUT)/src/
	@ echo "[javac] $@"
	@ mkdir -p $@
	@ touch $@
	$(JC) -d $@ -cp "$(DEPS)/*$(SEP)$(OUT)/src" $(JAVA_TST)

run: $(OUT)/src/
	java -ea -cp "$(DEPS)/*$(SEP)$(OUT)/src" trefoil2.Trefoil2 $(ARGS)

test: $(OUT)/tst/
	java -ea -cp "$(DEPS)/*$(SEP)$(OUT)/src$(SEP)$(OUT)/tst" org.junit.runner.JUnitCore Trefoil2Test

clean:
	@ echo "[clean]"
	@ rm -rf $(OUT)
