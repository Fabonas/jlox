# Makefile for jlox — build, run, and regenerate AST classes without IDEA.
# Usage:
#   make              # compile everything
#   make run          # start the REPL
#   make run f=foo.lox# run a script file
#   make ast          # regenerate Expr.java / Stmt.java
#   make clean        # remove compiled classes

# --- Project layout --------------------------------------------------------
SRC_DIR   := src
BUILD_DIR := build
TOOL_PKG  := com/tool
LOX_PKG   := com/lox

# Source files (the hand-written ones; Expr.java & Stmt.java are generated).
TOOL_SRC := $(SRC_DIR)/$(TOOL_PKG)/GenerateAst.java
LOX_SRC  := $(wildcard $(SRC_DIR)/$(LOX_PKG)/*.java)

# Generated AST files live alongside the other lox sources.
AST_GEN   := $(SRC_DIR)/$(LOX_PKG)/Expr.java $(SRC_DIR)/$(LOX_PKG)/Stmt.java

# Where GenerateAst writes its output (it takes an output directory argument).
AST_OUT_DIR := $(SRC_DIR)/$(LOX_PKG)

# All .java files to compile (including generated ones).
ALL_SRC := $(TOOL_SRC) $(LOX_SRC)

# --- Phony targets ---------------------------------------------------------
.PHONY: all run ast clean backup restore clear-backup help

all: $(BUILD_DIR)/.compiled

# --- Compilation -----------------------------------------------------------
# Compile tool + lox sources into build/. javac tracks source vs. class times,
# so only changed files are recompiled.
$(BUILD_DIR)/.compiled: $(ALL_SRC) | $(BUILD_DIR)
	javac -d $(BUILD_DIR) $(ALL_SRC)
	@touch $@

$(BUILD_DIR):
	@mkdir -p $@

# --- Run -------------------------------------------------------------------
# `make run`        → interactive REPL (stdin).
# `make run f=x.lox`→ run the given script file.
run: all
ifeq ($(f),)
	java -cp $(BUILD_DIR) com.lox.Lox
else
	java -cp $(BUILD_DIR) com.lox.Lox $(f)
endif

# --- Regenerate AST classes ------------------------------------------------
# Compile just the tool, run it to regenerate Expr.java/Stmt.java, then mark
# the main build stale so `make` recompiles with the new generated files.
ast: $(BUILD_DIR)/.tool-compiled
	java -cp $(BUILD_DIR) com.tool.GenerateAst $(AST_OUT_DIR)
	@rm -f $(BUILD_DIR)/.compiled

# Compile just the code generator into build/.
$(BUILD_DIR)/.tool-compiled: $(TOOL_SRC) | $(BUILD_DIR)
	javac -d $(BUILD_DIR) $(TOOL_SRC)
	@touch $@

# --- Backup / restore source for challenges --------------------------------
# `make backup`        → save the current src/ tree into backup/src/.
# `make restore`       → overwrite src/ with the contents of backup/src/.
# `make clear-backup`  → remove the backup/ directory.
BACKUP_DIR := backup

backup:
	@if [ -d "$(BACKUP_DIR)" ]; then \
		echo "Error: backup directory '$(BACKUP_DIR)/' already exists."; \
		echo "Run 'make restore' to revert src/ or 'make clear-backup' to remove it."; \
		exit 1; \
	fi; \
	mkdir -p "$(BACKUP_DIR)"; \
	cp -R src "$(BACKUP_DIR)"/; \
	find "$(BACKUP_DIR)" -type f -name '*.class' -delete; \
	echo "Backed up src/ -> $(BACKUP_DIR)/src/"

restore:
	@if [ ! -d "$(BACKUP_DIR)/src" ]; then \
		echo "Error: no backup found at $(BACKUP_DIR)/src/"; \
		echo "Run 'make backup' first."; \
		exit 1; \
	fi; \
	rm -rf src; \
	cp -R "$(BACKUP_DIR)/src" src; \
	echo "Restored src/ from $(BACKUP_DIR)/src/"

clear-backup:
	@rm -rf $(BACKUP_DIR)
	@echo "Removed $(BACKUP_DIR)/"

# --- Clean -----------------------------------------------------------------
clean:
	@rm -rf $(BUILD_DIR)

# --- Help ------------------------------------------------------------------
help:
	@echo "jlox Makefile targets:"
	@echo "  make              compile all sources"
	@echo "  make run          start the REPL"
	@echo "  make run f=x.lox  run a .lox script"
	@echo "  make ast          regenerate Expr.java / Stmt.java"
	@echo "  make backup       save src/ into backup/src/"
	@echo "  make restore      revert src/ from backup/src/"
	@echo "  make clear-backup remove backup/"
	@echo "  make clean        remove build/"
	@echo "  make help         this message"
