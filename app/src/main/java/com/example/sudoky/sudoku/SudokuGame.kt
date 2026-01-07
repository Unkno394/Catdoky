package com.example.sudoky.sudoku

data class SudokuGame(
    val size: Int,
    val boxSize: Int,
    val solution: IntArray,
    val puzzle: IntArray,
    val user: IntArray,
    val fixed: BooleanArray
)
