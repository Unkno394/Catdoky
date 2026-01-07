package com.example.sudoky.sudoku

class SudokuGenerator(
    private val solver: SudokuSolver = SudokuSolver(),
    private val random: java.util.Random = java.util.Random()
) {
    fun newGame(difficulty: Difficulty): SudokuGame {
        val (size, box) = when (difficulty) {
            Difficulty.NOVICE_9, Difficulty.STANDARD_9, Difficulty.HARD_9 -> 9 to 3
            Difficulty.STANDARD_16 -> 16 to 4
        }

        val solution = IntArray(size * size)
        require(solver.fillSolution(solution, size, box)) { "Не удалось сгенерировать решение" }

        val puzzle = if (size == 16) {
            // Упрощенная генерация для 16x16: убираем проверку уникальности, чтобы ускорить старт.
            solution.copyOf().also { removeCluesFast(it, targetClues = 160) }
        } else {
            solution.copyOf()
        }

        val cluesTarget = when (difficulty) {
            Difficulty.NOVICE_9 -> 48
            Difficulty.STANDARD_9 -> 36
            Difficulty.HARD_9 -> 28
            Difficulty.STANDARD_16 -> 140
        }

        if (size != 16) {
            makePuzzleUnique(puzzle, size, box, cluesTarget)
        }

        val fixed = BooleanArray(size * size) { puzzle[it] != 0 }
        val user = puzzle.copyOf()

        return SudokuGame(
            size = size,
            boxSize = box,
            solution = solution,
            puzzle = puzzle,
            user = user,
            fixed = fixed
        )
    }

    private fun removeCluesFast(puzzle: IntArray, targetClues: Int) {
        val indices = (0 until puzzle.size).shuffled(random)
        var clues = puzzle.count { it != 0 }
        for (idx in indices) {
            if (clues <= targetClues) break
            if (puzzle[idx] == 0) continue
            puzzle[idx] = 0
            clues--
        }
    }

    private fun makePuzzleUnique(puzzle: IntArray, size: Int, boxSize: Int, cluesTarget: Int) {
        val indices = (0 until size * size).toMutableList()
        indices.shuffle(random)

        var clues = puzzle.count { it != 0 }

        for (idx in indices) {
            if (clues <= cluesTarget) break

            val backup = puzzle[idx]
            if (backup == 0) continue

            puzzle[idx] = 0

            val copy = puzzle.copyOf()
            val solCount = solver.countSolutions(copy, size, boxSize, limit = 2)

            if (solCount != 1) {
                puzzle[idx] = backup
            } else {
                clues--
            }
        }
    }
}
