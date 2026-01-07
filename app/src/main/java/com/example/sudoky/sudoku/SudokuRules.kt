package com.example.sudoky.sudoku

object SudokuRules {
    fun canPlace(grid: IntArray, size: Int, boxSize: Int, r: Int, c: Int, v: Int): Boolean {
        if (v !in 1..size) return false

        for (cc in 0 until size) {
            if (cc != c && grid[r * size + cc] == v) return false
        }

        for (rr in 0 until size) {
            if (rr != r && grid[rr * size + c] == v) return false
        }

        val br = (r / boxSize) * boxSize
        val bc = (c / boxSize) * boxSize
        for (rr in br until br + boxSize) {
            for (cc in bc until bc + boxSize) {
                if ((rr != r || cc != c) && grid[rr * size + cc] == v) return false
            }
        }
        return true
    }
}
