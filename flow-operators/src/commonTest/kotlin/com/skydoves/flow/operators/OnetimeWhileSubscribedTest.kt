/*
 * Designed and developed by 2025 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.skydoves.flow.operators

import app.cash.turbine.test
import com.skydoves.flow.operators.onetime.onetimeStateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.job
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OnetimeWhileSubscribedTest {

  @Test
  fun `WhileSubscribed always emit values whenever collected`() = runTest {
    var executedCount = 0

    val stateFlow = flow {
      executedCount++
      emit(executedCount)
    }.shareIn(
      scope = backgroundScope,
      started = SharingStarted.WhileSubscribed(100),
    )

    assertEquals(0, executedCount)
    stateFlow.first()
    assertEquals(1, executedCount)
    delay(200)
    stateFlow.first()
    assertEquals(2, executedCount)

    coroutineContext.job.cancelChildren()
  }

  @Test
  fun `onetimeStateIn should emit a value only a single time`() = runTest {
    var executedCount = 0

    val mutableStateFlow = MutableStateFlow(0)
    val stateFlow = mutableStateFlow.mapLatest {
      ++executedCount
      executedCount.toString()
    }.onetimeStateIn(
      scope = backgroundScope,
      stopTimeout = 100,
      initialValue = "initial",
    )

    stateFlow.test {
      assertEquals(0, executedCount)
      assertEquals("initial", awaitItem())

      delay(200)
      mutableStateFlow.value = 1

      assertEquals(1, executedCount)
      assertEquals("1", awaitItem())

      delay(200)
      mutableStateFlow.value = 2

      delay(200)
      mutableStateFlow.value = 3

      expectNoEvents()
    }

    stateFlow.test {
      assertEquals(1, executedCount)
      assertEquals("1", awaitItem())
      expectNoEvents()
    }

    coroutineContext.job.cancelChildren()
  }
}
