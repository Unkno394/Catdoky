package com.example.sudoky.sudoku

class SudokuSolver(private val random: java.util.Random = java.util.Random()) {

    fun fillSolution(grid: IntArray, size: Int, boxSize: Int): Boolean {
        val idx = grid.indexOfFirst { it == 0 }
        if (idx == -1) return true

        val r = idx / size
        val c = idx % size

        val nums = (1..size).toMutableList()
        nums.shuffle(random)

        for (v in nums) {
            if (SudokuRules.canPlace(grid, size, boxSize, r, c, v)) {
                grid[idx] = v
                if (fillSolution(grid, size, boxSize)) return true
                grid[idx] = 0
            }
        }
        return false
    }

    fun countSolutions(grid: IntArray, size: Int, boxSize: Int, limit: Int = 2): Int {
        var count = 0

        fun dfs(): Boolean {
            val idx = grid.indexOfFirst { it == 0 }
            if (idx == -1) {
                count++
                return count >= limit
            }

            val r = idx / size
            val c = idx % size

            for (v in 1..size) {
                if (SudokuRules.canPlace(grid, size, boxSize, r, c, v)) {
                    grid[idx] = v
                    val stop = dfs()
                    grid[idx] = 0
                    if (stop) return true
                }
            }
            return false
        }

        dfs()
        return count
    }
}
