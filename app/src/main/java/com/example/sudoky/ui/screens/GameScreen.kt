package com.example.sudoky.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sudoky.sudoku.Difficulty
import com.example.sudoky.sudoku.MoveResult
import com.example.sudoky.sudoku.SudokuEngine
import com.example.sudoky.sudoku.SudokuGame
import com.example.sudoky.sudoku.SudokuGenerator
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.platform.LocalContext
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.Build
import android.content.Context
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pets
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.sudoky.R
import com.example.sudoky.data.ProgressPreferences
import com.example.sudoky.data.xpForResult

private const val MAX_HINTS = 3

class GameViewModel : ViewModel() {
    private val generator = SudokuGenerator()
    private val engine = SudokuEngine(strictCheck = false)

    private val _game = mutableStateOf<SudokuGame?>(null)
    val game: SudokuGame?
        get() = _game.value

    private val _lastMove = mutableStateOf<MoveResult?>(null)
    val lastMove: MoveResult?
        get() = _lastMove.value
    private val _lastInputCell = mutableStateOf<Pair<Int, Int>?>(null)
    val lastInputCell: Pair<Int, Int>?
        get() = _lastInputCell.value
    private val _failedCell = mutableStateOf<Pair<Int, Int>?>(null)
    val failedCell: Pair<Int, Int>?
        get() = _failedCell.value
    private val _failedValue = mutableStateOf<Int?>(null)
    val failedValue: Int?
        get() = _failedValue.value
    private val _errors = mutableStateOf(0)
    val errors: Int
        get() = _errors.value
    private val _candidates = mutableStateOf<List<Set<Int>>>(emptyList())
    val candidates: List<Set<Int>>
        get() = _candidates.value
    private val _pencilMode = mutableStateOf(false)
    val pencilMode: Boolean
        get() = _pencilMode.value
    private val _hintsUsed = mutableStateOf(0)
    val hintsUsed: Int
        get() = _hintsUsed.value
    private val _elapsedSeconds = mutableStateOf(0)
    val elapsedSeconds: Int
        get() = _elapsedSeconds.value

    fun startNewGame(diff: Difficulty) {
        val newGame = generator.newGame(diff)
        _game.value = newGame
        _lastMove.value = null
        _lastInputCell.value = null
        _failedCell.value = null
        _failedValue.value = null
        _errors.value = 0
        _pencilMode.value = false
        _hintsUsed.value = 0
        _elapsedSeconds.value = 0
        _candidates.value = List(newGame.size * newGame.size) { emptySet() }
    }

    fun onInput(r: Int, c: Int, v: Int) {
        val current = _game.value ?: return
        val idx = r * current.size + c
        if (current.fixed[idx]) return
        if (current.user[idx] != 0 && current.user[idx] == current.solution[idx]) return
        val result = engine.trySet(current, r, c, v)
        _lastMove.value = result
        _lastInputCell.value = r to c
        if (result.accepted) {
            _failedCell.value = null
            _failedValue.value = null
        } else {
            _failedCell.value = r to c
            _failedValue.value = v
            _errors.value = _errors.value + 1
        }
        if (result.accepted) {
            clearCandidates(r, c)
        }
        _game.value = current.copy(user = current.user.copyOf())
    }

    fun togglePencilMode() {
        _pencilMode.value = !_pencilMode.value
    }

    fun toggleCandidate(r: Int, c: Int, v: Int) {
        val current = _game.value ?: return
        val idx = r * current.size + c
        if (current.fixed[idx]) return
        if (current.user[idx] != 0) return
        val list = _candidates.value.toMutableList()
        val existing = list[idx]
        val updated = if (existing.contains(v)) {
            existing - v
        } else {
            existing + v
        }
        list[idx] = updated
        _candidates.value = list
    }

    fun clearCandidates(r: Int, c: Int) {
        val current = _game.value ?: return
        val idx = r * current.size + c
        val list = _candidates.value.toMutableList()
        if (list[idx].isNotEmpty()) {
            list[idx] = emptySet()
            _candidates.value = list
        }
    }

    fun clearCell(r: Int, c: Int) {
        val current = _game.value ?: return
        val idx = r * current.size + c
        if (current.fixed[idx]) return
        if (current.user[idx] != 0 && current.user[idx] == current.solution[idx]) return
        if (current.user[idx] == 0) {
            clearCandidates(r, c)
            return
        }
        current.user[idx] = 0
        clearCandidates(r, c)
        if (_failedCell.value == r to c) {
            _failedCell.value = null
            _failedValue.value = null
        }
        _lastMove.value = null
        _game.value = current.copy(user = current.user.copyOf())
    }

    fun incrementTime() {
        _elapsedSeconds.value = _elapsedSeconds.value + 1
    }

    fun applyHint(selected: Pair<Int, Int>?): Boolean {
        val current = _game.value ?: return false
        if (_hintsUsed.value >= MAX_HINTS) return false
        val size = current.size
        fun canHintAt(r: Int, c: Int): Boolean {
            val idx = r * size + c
            if (current.fixed[idx]) return false
            val currentValue = current.user[idx]
            val solutionValue = current.solution[idx]
            return currentValue == 0 || currentValue != solutionValue
        }

        val target = selected?.takeIf { canHintAt(it.first, it.second) }
            ?: run {
                (0 until size * size)
                    .asSequence()
                    .map { it / size to it % size }
                    .firstOrNull { (r, c) -> canHintAt(r, c) }
            }
            ?: return false

        val (r, c) = target
        val idx = r * size + c
        current.user[idx] = current.solution[idx]
        clearCandidates(r, c)
        _game.value = current.copy(user = current.user.copyOf())
        _hintsUsed.value = _hintsUsed.value + 1
        return true
    }
}

@Composable
fun GameScreen(
    difficulty: Difficulty,
    onBack: () -> Unit,
    onWin: (elapsedSeconds: Int) -> Unit,
    onLose: (elapsedSeconds: Int) -> Unit,
    viewModel: GameViewModel = viewModel()
) {
    var paused by remember { mutableStateOf(false) }
    var hasNavigated by remember { mutableStateOf(false) }
    LaunchedEffect(difficulty) {
        viewModel.startNewGame(difficulty)
        paused = false
        hasNavigated = false
    }

    val game = viewModel.game
    val move = viewModel.lastMove
    val failedCell = viewModel.failedCell
    val failedValue = viewModel.failedValue
    val errors = viewModel.errors
    val candidates = viewModel.candidates
    val pencilMode = viewModel.pencilMode
    val hintsUsed = viewModel.hintsUsed
    val elapsedSeconds = viewModel.elapsedSeconds
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val context = LocalContext.current
    val progressPrefs = remember { ProgressPreferences(context) }
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }
    var selected by remember(game) { mutableStateOf<Pair<Int, Int>?>(null) }
    val maxErrors = 3
    val gameOver = errors >= maxErrors
    val gameWon = game?.let { it.user.contentEquals(it.solution) } == true
    val actionButtonColor = Color(0xFF949CCD)
    val pauseComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.cat_pause)
    )
    val pauseAnimProgress by animateLottieCompositionAsState(
        composition = pauseComposition,
        iterations = LottieConstants.IterateForever,
        isPlaying = paused && !gameOver && !gameWon
    )
    val tutorialShown by progressPrefs.tutorialShownFlow.collectAsState(initial = false)
    var showTutorial by remember { mutableStateOf(false) }
    var tutorialStep by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    var pauseRect by remember { mutableStateOf<Rect?>(null) }
    var pencilRect by remember { mutableStateOf<Rect?>(null) }
    var hintRect by remember { mutableStateOf<Rect?>(null) }
    var errorsRect by remember { mutableStateOf<Rect?>(null) }
    var gridRect by remember { mutableStateOf<Rect?>(null) }
    var keypadRect by remember { mutableStateOf<Rect?>(null) }

    LaunchedEffect(tutorialShown) {
        if (!tutorialShown) {
            showTutorial = true
            tutorialStep = 0
        }
    }

    LaunchedEffect(move) {
        if (move != null && !move.accepted) {
            vibrator?.let { vib ->
                runCatching {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vib.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vib.vibrate(60)
                    }
                }
            }
        }
    }

    LaunchedEffect(paused, gameOver, game, gameWon) {
        while (isActive) {
            if (!paused && !gameOver && !gameWon && game != null) {
                viewModel.incrementTime()
            }
            delay(1000)
        }
    }

    LaunchedEffect(gameOver, gameWon) {
        if (hasNavigated) return@LaunchedEffect
        when {
            gameOver -> {
                paused = false
                hasNavigated = true
                val xp = xpForResult(difficulty, elapsedSeconds, isWin = false, hintsUsed = hintsUsed)
                progressPrefs.addXp(xp)
                onLose(elapsedSeconds)
            }
            gameWon -> {
                paused = false
                hasNavigated = true
                val xp = xpForResult(difficulty, elapsedSeconds, isWin = true, hintsUsed = hintsUsed)
                progressPrefs.addXp(xp)
                onWin(elapsedSeconds)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 90.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-20).dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { paused = !paused },
                        enabled = !gameOver && !gameWon,
                        modifier = Modifier.onGloballyPositioned { coords ->
                            pauseRect = coords.boundsInRoot()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = actionButtonColor
                        )
                    ) {
                        Icon(
                            imageVector = if (paused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                            contentDescription = if (paused) "Продолжить" else "Пауза"
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = formatElapsedTime(elapsedSeconds),
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.size(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = actionButtonColor
                            )
                        ) {
                            Text("Назад")
                        }
                        Spacer(modifier = Modifier.size(8.dp))
                        Button(
                            onClick = { viewModel.togglePencilMode() },
                            modifier = Modifier.onGloballyPositioned { coords ->
                                pencilRect = coords.boundsInRoot()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = actionButtonColor
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Карандаш"
                            )
                        }
                        Spacer(modifier = Modifier.size(8.dp))
                        Button(
                            onClick = { viewModel.applyHint(selected) },
                            enabled = hintsUsed < MAX_HINTS && !gameOver && !gameWon,
                            modifier = Modifier.onGloballyPositioned { coords ->
                                hintRect = coords.boundsInRoot()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = actionButtonColor
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lightbulb,
                                contentDescription = "Подсказка ${hintsUsed}/${MAX_HINTS}"
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.onGloballyPositioned { coords ->
                            errorsRect = coords.boundsInRoot()
                        }
                    ) {
                        repeat(maxErrors) { i ->
                            val active = i >= errors
                            Icon(
                                imageVector = Icons.Filled.Pets,
                                contentDescription = null,
                                tint = if (active) {
                                    Color(0xFFB39DDB)
                                } else {
                                    Color.White.copy(alpha = 0.25f)
                                },
                                modifier = Modifier
                                    .size(22.dp)
                                    .padding(start = 6.dp)
                            )
                        }
                    }
                }
            }

            if (game == null) {
                Text("Загрузка головоломки...")
                return
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                // intentionally left minimal; meta info скрыто
            }

            Box(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { coords ->
                            gridRect = coords.boundsInRoot()
                        }
                ) {
                    SudokuGrid(
                        game = game,
                        modifier = Modifier.fillMaxSize(),
                        selected = selected,
                        candidates = candidates,
                        lastFailedCell = failedCell,
                        lastFailedValue = failedValue,
                        isDark = isDark,
                    onCellClick = { row, col ->
                        if (!paused && !gameOver && !gameWon) {
                            selected = row to col
                        }
                    },
                    onCellLongPress = { row, col ->
                        if (!paused && !gameOver && !gameWon) {
                            viewModel.clearCell(row, col)
                        }
                    }
                )
            }
                if (paused && !gameOver && !gameWon) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.75f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.offset(y = 30.dp)
                        ) {
                            Text(
                                text = "Пауза...",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )
                            LottieAnimation(
                                composition = pauseComposition,
                                progress = { pauseAnimProgress },
                                modifier = Modifier.fillMaxWidth(0.7f)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                move?.let {
                    val msg = when {
                        !it.accepted && it.violatesRules -> "Нарушает правила"
                        !it.accepted && it.wrongAgainstSolution -> "Не совпадает с решением"
                        it.accepted -> "Ход принят"
                        else -> ""
                    }
                    if (msg.isNotEmpty()) {
                        Text(msg, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp)
                    .onGloballyPositioned { coords ->
                        keypadRect = coords.boundsInRoot()
                    },
                contentAlignment = Alignment.Center
            ) {
                NumberPad(
                    size = game.size,
                    enabled = selected != null &&
                        selected?.let { (r, c) ->
                            val idx = r * game.size + c
                            !game.fixed[idx] &&
                                !(game.user[idx] != 0 && game.user[idx] == game.solution[idx])
                        } == true &&
                        !paused &&
                        !gameOver &&
                        !gameWon,
                    onNumber = { value ->
                        selected?.let { (r, c) ->
                            val idx = r * game.size + c
                            if (game.fixed[idx]) return@let
                            if (pencilMode) {
                                viewModel.toggleCandidate(r, c, value)
                            } else {
                                viewModel.onInput(r, c, value)
                            }
                        }
                    },
                    isDark = isDark
                )
            }
        }

        if (showTutorial) {
            val steps = listOf(
                CoachStep(
                    title = "Кот спит 😴",
                    text = "Нажми паузу — таймер остановится, поле заблокируется.",
                    target = pauseRect
                ),
                CoachStep(
                    title = "Карандаш ✏",
                    text = "Режим заметок: можно ставить маленькие варианты в клетке.",
                    target = pencilRect
                ),
                CoachStep(
                    title = "Подсказка 💡",
                    text = "Вставляет верное число. На игру всего 3 подсказки.",
                    target = hintRect
                ),
                CoachStep(
                    title = "Ошибки 🐾",
                    text = "Лапки — это попытки. Когда лапки кончатся — проигрыш.",
                    target = errorsRect
                ),
                CoachStep(
                    title = "Сетка",
                    text = "Нажми клетку, чтобы выбрать. Зажми — число сотрётся.",
                    target = gridRect
                ),
                CoachStep(
                    title = "Клавиатура",
                    text = "Выбирай число снизу. В 16×16 свайпай вправо, чтобы увидеть A–G.",
                    target = keypadRect
                )
            )
            val step = steps.getOrNull(tutorialStep)
            CoachMarkOverlay(
                target = step?.target,
                title = step?.title.orEmpty(),
                text = step?.text.orEmpty(),
                step = tutorialStep + 1,
                steps = steps.size,
                onNext = {
                    if (tutorialStep >= steps.lastIndex) {
                        scope.launch { progressPrefs.setTutorialShown(true) }
                        showTutorial = false
                    } else {
                        tutorialStep += 1
                    }
                },
                onSkip = {
                    scope.launch { progressPrefs.setTutorialShown(true) }
                    showTutorial = false
                }
            )
        }
    }
}

private data class CoachStep(
    val title: String,
    val text: String,
    val target: Rect?
)

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun CoachMarkOverlay(
    target: Rect?,
    title: String,
    text: String,
    step: Int,
    steps: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    if (target == null) return

    val accent = Color(0xFF949CCD)
    val density = LocalDensity.current

    var cardHeightPx by remember { mutableStateOf(0f) }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val screenW = constraints.maxWidth.toFloat()
        val screenH = constraints.maxHeight.toFloat()

        val padPx = with(density) { 12.dp.toPx() }
        val gapPx = with(density) { 14.dp.toPx() }
        val sidePadPx = with(density) { 18.dp.toPx() }
        val cornerPx = with(density) { 18.dp.toPx() }

        val highlight = Rect(
            left = (target.left - padPx).coerceAtLeast(0f),
            top = (target.top - padPx).coerceAtLeast(0f),
            right = (target.right + padPx).coerceAtMost(screenW),
            bottom = (target.bottom + padPx).coerceAtMost(screenH)
        )

        val spaceAbove = highlight.top
        val spaceBelow = screenH - highlight.bottom

        val canPlaceAbove = cardHeightPx > 0f && spaceAbove >= (cardHeightPx + gapPx)
        val canPlaceBelow = cardHeightPx > 0f && spaceBelow >= (cardHeightPx + gapPx)

        val placeAbove = when {
            canPlaceAbove && !canPlaceBelow -> true
            !canPlaceAbove && canPlaceBelow -> false
            canPlaceAbove && canPlaceBelow -> true
            else -> false
        }

        val cardMaxWidthPx = with(density) { 360.dp.toPx() }
        val cardWidthPx = minOf(screenW - sidePadPx * 2f, cardMaxWidthPx)

        val desiredLeft = highlight.center.x - cardWidthPx / 2f
        val cardLeftPx = desiredLeft.coerceIn(sidePadPx, screenW - sidePadPx - cardWidthPx)

        val cardTopPx = when {
            placeAbove -> (highlight.top - gapPx - cardHeightPx).coerceAtLeast(sidePadPx)
            else -> (highlight.bottom + gapPx).coerceAtMost(screenH - sidePadPx - cardHeightPx)
        }

        Canvas(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = 0.99f }
                .pointerInteropFilter { true }
        ) {
            drawRect(Color.Black.copy(alpha = 0.78f))

            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(highlight.left, highlight.top),
                size = Size(highlight.width, highlight.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerPx, cornerPx),
                blendMode = BlendMode.Clear
            )

            drawRoundRect(
                color = accent.copy(alpha = 0.95f),
                topLeft = Offset(highlight.left, highlight.top),
                size = Size(highlight.width, highlight.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerPx, cornerPx),
                style = Stroke(width = with(density) { 2.5.dp.toPx() })
            )
            drawRoundRect(
                color = accent.copy(alpha = 0.22f),
                topLeft = Offset(
                    highlight.left - with(density) { 6.dp.toPx() },
                    highlight.top - with(density) { 6.dp.toPx() }
                ),
                size = Size(
                    highlight.width + with(density) { 12.dp.toPx() },
                    highlight.height + with(density) { 12.dp.toPx() }
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                    cornerPx + with(density) { 10.dp.toPx() },
                    cornerPx + with(density) { 10.dp.toPx() }
                ),
                style = Stroke(width = with(density) { 10.dp.toPx() })
            )
        }

        Box(Modifier.fillMaxSize()) {
            if (cardHeightPx > 0f) {
                val arrowW = 18.dp
                val arrowH = 10.dp

                Canvas(
                    modifier = Modifier
                        .offset(
                            x = with(density) { (cardLeftPx + cardWidthPx / 2f).toDp() } - arrowW / 2,
                            y = with(density) {
                                (if (placeAbove) (highlight.top - gapPx - with(density) { arrowH.toPx() })
                                 else (highlight.bottom + gapPx)).toDp()
                            }
                        )
                        .size(arrowW, arrowH)
                ) {
                    val path = Path().apply {
                        if (placeAbove) {
                            moveTo(0f, 0f)
                            lineTo(size.width, 0f)
                            lineTo(size.width / 2f, size.height)
                            close()
                        } else {
                            moveTo(size.width / 2f, 0f)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                    }
                    drawPath(path, color = Color(0xFF2A2A2E))
                }
            }

            Card(
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .offset(
                        x = with(density) { cardLeftPx.toDp() },
                        y = with(density) { cardTopPx.toDp() }
                    )
                    .onGloballyPositioned { coords ->
                        cardHeightPx = coords.size.height.toFloat()
                    },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2E))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(accent.copy(alpha = 0.22f), RoundedCornerShape(999.dp))
                                .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$step/$steps",
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        Text(
                            text = title,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Text(
                        text = text,
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onSkip) {
                            Text("Пропустить", color = Color.White.copy(alpha = 0.8f))
                        }
                        Button(
                            onClick = onNext,
                            colors = ButtonDefaults.buttonColors(containerColor = accent)
                        ) {
                            Text(if (step == steps) "Готово" else "Далее")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SudokuGrid(
    game: SudokuGame,
    modifier: Modifier = Modifier,
    selected: Pair<Int, Int>?,
    candidates: List<Set<Int>>,
    lastFailedCell: Pair<Int, Int>?,
    lastFailedValue: Int?,
    isDark: Boolean,
    onCellClick: (row: Int, col: Int) -> Unit,
    onCellLongPress: (row: Int, col: Int) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        val boxSize = game.boxSize
        val boardColor = if (isDark) Color(0xFF2A2A2E) else Color.White
        val selectedIsLocked = selected?.let { (r, c) ->
            val idx = r * game.size + c
            game.fixed[idx] || (game.user[idx] != 0 && game.user[idx] == game.solution[idx])
        } == true
        val selectedValue = selected?.let { (r, c) ->
            val idx = r * game.size + c
            if (lastFailedCell?.first == r && lastFailedCell.second == c && lastFailedValue != null) {
                lastFailedValue
            } else {
                game.user[idx]
            }
        } ?: 0

        // 1) клетки без рамок
        Column(modifier = Modifier.fillMaxSize()) {
            game.user.toList().chunked(game.size).forEachIndexed { rowIndex, row ->
                Row(modifier = Modifier.weight(1f)) {
                    row.forEachIndexed { colIndex, value ->
                        val fixed = game.fixed[rowIndex * game.size + colIndex]
                        val isSelected = selected?.first == rowIndex && selected.second == colIndex
                        val isFailed = lastFailedCell?.first == rowIndex && lastFailedCell.second == colIndex
                        val displayValue = if (isFailed && lastFailedValue != null) lastFailedValue else value
                        val sameRow = selected?.first == rowIndex
                        val sameCol = selected?.second == colIndex
                        val sameBlock = selected?.let {
                            (it.first / game.boxSize == rowIndex / game.boxSize) &&
                            (it.second / game.boxSize == colIndex / game.boxSize)
                        } == true
                        val highlightSameNumber = selectedValue != 0 && displayValue == selectedValue
                        val candidatesForCell = candidates.getOrNull(rowIndex * game.size + colIndex) ?: emptySet()
                        SudokuCell(
                            value = displayValue,
                            gridSize = game.size,
                            fixed = fixed,
                            selected = isSelected && !selectedIsLocked,
                            failed = isFailed,
                            highlightRowOrCol = !isSelected && !selectedIsLocked && (sameRow || sameCol),
                            highlightBlock = !isSelected && !selectedIsLocked && sameBlock,
                            highlightSameNumber = !isSelected && highlightSameNumber,
                            baseColor = boardColor,
                            isDark = isDark,
                            candidates = candidatesForCell,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                                onClick = { onCellClick(rowIndex, colIndex) },
                            onLongPress = { onCellLongPress(rowIndex, colIndex) }
                        )
                    }
                }
            }
        }

        // 2) сетка поверх клеток
        val thinStroke = if (game.size == 16) 0.55.dp else 0.8.dp
        val thickStroke = if (game.size == 16) 2.0.dp else 3.2.dp
        val thinColor = if (isDark) {
            Color.White.copy(alpha = 0.35f)
        } else {
            Color.Black.copy(alpha = 0.35f)
        }
        val thickColor = if (isDark) {
            Color.White.copy(alpha = 0.95f)
        } else {
            Color.Black.copy(alpha = 0.95f)
        }
        val selectedColor = MaterialTheme.colorScheme.primary
        val failedColor = MaterialTheme.colorScheme.error
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellSize = size.width / game.size
            for (i in 0..game.size) {
                val isOuter = i == 0 || i == game.size
                val isBlockBorder = i % boxSize == 0
                val stroke = if (isOuter || isBlockBorder) thickStroke.toPx() else thinStroke.toPx()
                val color = if (isOuter || isBlockBorder) thickColor else thinColor
                val offset = i * cellSize
                drawLine(
                    color = color,
                    start = Offset(offset, 0f),
                    end = Offset(offset, size.height),
                    strokeWidth = stroke
                )
                drawLine(
                    color = color,
                    start = Offset(0f, offset),
                    end = Offset(size.width, offset),
                    strokeWidth = stroke
                )
            }
        }

        // 3) рамки выбранной/ошибочной поверх сетки
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cell = size.width / game.size

            selected?.let { (r, c) ->
                drawRect(
                    color = selectedColor,
                    topLeft = Offset(c * cell, r * cell),
                    size = Size(cell, cell),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            lastFailedCell?.let { (r, c) ->
                drawRect(
                    color = failedColor,
                    topLeft = Offset(c * cell, r * cell),
                    size = Size(cell, cell),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SudokuCell(
    value: Int,
    gridSize: Int,
    fixed: Boolean,
    selected: Boolean,
    failed: Boolean,
    highlightRowOrCol: Boolean,
    highlightBlock: Boolean,
    highlightSameNumber: Boolean,
    baseColor: Color,
    isDark: Boolean,
    candidates: Set<Int>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val rowHighlight = if (isDark) {
        Color(0xFFB39DDB).copy(alpha = 0.55f)
    } else {
        Color(0xFFB39DDB).copy(alpha = 0.20f)
    }
    val blockHighlight = if (isDark) {
        Color(0xFFB39DDB).copy(alpha = 0.35f)
    } else {
        Color(0xFFB39DDB).copy(alpha = 0.12f)
    }
    val selectedHighlight = Color(0xFFB39DDB).copy(
        alpha = if (isDark) 0.60f else 0.20f
    )
    val sameNumberHighlight = if (isDark) {
        Color(0xFF7BCF9B).copy(alpha = 0.60f)
    } else {
        Color(0xFF7BCF9B).copy(alpha = 0.20f)
    }
    val targetBg = when {
        failed -> MaterialTheme.colorScheme.error.copy(alpha = 0.25f)
        selected -> selectedHighlight
        highlightSameNumber -> sameNumberHighlight
        highlightRowOrCol -> rowHighlight
        highlightBlock -> blockHighlight
        else -> baseColor
    }
    val bg by animateColorAsState(
        targetValue = targetBg,
        animationSpec = tween(durationMillis = 180),
        label = "cellBg"
    )
    val textColor = if (isDark) Color.White else Color.Black
    Box(
        modifier = modifier
            .background(bg)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            ),
        contentAlignment = Alignment.Center
    ) {
        val fontSize = when (gridSize) {
            16 -> 14.sp
            else -> 22.sp
        }
        if (value == 0 && candidates.isNotEmpty()) {
            val cols = if (gridSize == 16) 4 else 3
            val rows = candidates.sorted().chunked(cols)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                rows.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        row.forEach { v ->
                            Text(
                                text = formatCellValue(v, gridSize),
                                style = TextStyle(fontSize = 10.sp),
                                color = textColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size < cols) {
                            repeat(cols - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        } else {
            Text(
                text = formatCellValue(value, gridSize),
                style = TextStyle(fontSize = fontSize),
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatCellValue(value: Int, gridSize: Int): String {
    if (value == 0) return ""
    if (gridSize == 16 && value >= 10) {
        return ('A' + (value - 10)).toString()
    }
    return value.toString()
}

private fun formatElapsedTime(seconds: Int): String {
    val hours = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return "%02d:%02d:%02d".format(hours, mins, secs)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NumberPad(
    size: Int,
    enabled: Boolean,
    onNumber: (Int) -> Unit,
    isDark: Boolean
) {
    val numbers = (1..size).toList()
    val pages = numbers.chunked(9)
    val btnColor = Color(0xFF949CCD)
    val textColor = if (isDark) Color.White else Color.Black
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (pages.size == 1) {
            NumberPadGrid(
                numbers = pages.first(),
                gridSize = size,
                enabled = enabled,
                onNumber = onNumber,
                btnColor = btnColor,
                textColor = textColor
            )
        } else {
            val pagerState = rememberPagerState { pages.size }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                NumberPadGrid(
                    numbers = pages[page],
                    gridSize = size,
                    enabled = enabled,
                    onNumber = onNumber,
                    btnColor = btnColor,
                    textColor = textColor
                )
            }
        }
    }
}

@Composable
private fun NumberPadGrid(
    numbers: List<Int>,
    gridSize: Int,
    enabled: Boolean,
    onNumber: (Int) -> Unit,
    btnColor: Color,
    textColor: Color
) {
    val rows = numbers.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { n ->
                    Button(
                        onClick = { onNumber(n) },
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        enabled = enabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = btnColor,
                            contentColor = textColor
                        )
                    ) {
                        Text(
                            text = formatCellValue(n, gridSize),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
                if (row.size < 3) {
                    repeat(3 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
