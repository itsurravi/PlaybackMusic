package com.ravisharma.playbackmusic.new_work.services

import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.source.ShuffleOrder
import java.util.Arrays
import java.util.Random

@UnstableApi
class CustomShuffleOrder private constructor(
    private val shuffled: IntArray,
    private val random: Random
) : ShuffleOrder {
    private val indexInShuffled = IntArray(shuffled.size)

    /**
     * Creates an instance with a specified length and sets the current index as the first item.
     *
     * @param length       The length of the shuffle order.
     * @param currentIndex The index of the currently playing item.
     */
    constructor(length: Int, currentIndex: Int) : this(length, currentIndex, Random())

    /**
     * Creates an instance with a specified length, the specified random seed, and the current index as the first item.
     *
     * @param length       The length of the shuffle order.
     * @param currentIndex The index of the currently playing item.
     * @param randomSeed   A random seed.
     */
    constructor(length: Int, currentIndex: Int, randomSeed: Long) : this(
        length,
        currentIndex,
        Random(randomSeed)
    )

    private constructor(length: Int, currentIndex: Int, random: Random) : this(
        createShuffledList(
            length,
            currentIndex,
            random
        ), random
    )

    init {
        for (i in shuffled.indices) {
            indexInShuffled[shuffled[i]] = i
        }
    }

    override fun getLength(): Int {
        return shuffled.size
    }

    override fun getNextIndex(index: Int): Int {
        var shuffledIndex = indexInShuffled[index]
        return if (++shuffledIndex < shuffled.size) shuffled[shuffledIndex] else C.INDEX_UNSET
    }

    override fun getPreviousIndex(index: Int): Int {
        var shuffledIndex = indexInShuffled[index]
        return if (--shuffledIndex >= 0) shuffled[shuffledIndex] else C.INDEX_UNSET
    }

    override fun getLastIndex(): Int {
        return if (shuffled.isNotEmpty()) shuffled[shuffled.size - 1] else C.INDEX_UNSET
    }

    override fun getFirstIndex(): Int {
        return if (shuffled.isNotEmpty()) shuffled[0] else C.INDEX_UNSET
    }

    override fun cloneAndInsert(insertionIndex: Int, insertionCount: Int): ShuffleOrder {
        val insertionPoints = IntArray(insertionCount)
        val insertionValues = IntArray(insertionCount)
        for (i in 0 until insertionCount) {
            insertionPoints[i] = random.nextInt(shuffled.size + 1)
            val swapIndex = random.nextInt(i + 1)
            insertionValues[i] = insertionValues[swapIndex]
            insertionValues[swapIndex] = i + insertionIndex
        }
        Arrays.sort(insertionPoints)
        val newShuffled = IntArray(shuffled.size + insertionCount)
        var indexInOldShuffled = 0
        var indexInInsertionList = 0
        for (i in 0 until shuffled.size + insertionCount) {
            if (indexInInsertionList < insertionCount
                && indexInOldShuffled == insertionPoints[indexInInsertionList]
            ) {
                newShuffled[i] = insertionValues[indexInInsertionList++]
            } else {
                newShuffled[i] = shuffled[indexInOldShuffled++]
                if (newShuffled[i] >= insertionIndex) {
                    newShuffled[i] += insertionCount
                }
            }
        }
        return CustomShuffleOrder(newShuffled, Random(random.nextLong()))
    }

    override fun cloneAndRemove(indexFrom: Int, indexToExclusive: Int): ShuffleOrder {
        val numberOfElementsToRemove = indexToExclusive - indexFrom
        val newShuffled = IntArray(shuffled.size - numberOfElementsToRemove)
        var foundElementsCount = 0
        for (i in shuffled.indices) {
            if (shuffled[i] in indexFrom..<indexToExclusive) {
                foundElementsCount++
            } else {
                newShuffled[i - foundElementsCount] =
                    if (shuffled[i] >= indexFrom) shuffled[i] - numberOfElementsToRemove else shuffled[i]
            }
        }
        return CustomShuffleOrder(newShuffled, Random(random.nextLong()))
    }

    override fun cloneAndClear(): ShuffleOrder {
        return CustomShuffleOrder( /* length= */0, 0, Random(random.nextLong()))
    }

    companion object {
        private fun createShuffledList(length: Int, currentIndex: Int, random: Random): IntArray {
            val shuffled = IntArray(length)
            for (i in 0 until length) {
                shuffled[i] = i
            }
            // Shuffle the array
            for (i in length - 1 downTo 1) {
                val swapIndex = random.nextInt(i + 1)
                val temp = shuffled[swapIndex]
                shuffled[swapIndex] = shuffled[i]
                shuffled[i] = temp
            }
            // Ensure currentIndex is at the start
            val temp = shuffled[0]
            var currentIndexPos = -1
            for (i in 0 until length) {
                if (shuffled[i] == currentIndex) {
                    currentIndexPos = i
                    break
                }
            }
            if (currentIndexPos != -1) {
                shuffled[currentIndexPos] = temp
                shuffled[0] = currentIndex
            }
            return shuffled
        }
    }
}
