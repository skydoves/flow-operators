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
package com.skydoves.flow.operators.restartable

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingCommand
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn

/**
 * `restartableStateIn` returns a [RestartableStateFlow] that implements both [StateFlow] and
 * [Restartable], and is designed to restart the emission of the upstream flow. It functions just
 * like a regular [StateFlow], but with the added ability to restart the upstream emission when needed.
 *
 * @param scope the coroutine scope in which sharing is started.
 * @param started the strategy that controls when sharing is started and stopped.
 * @param initialValue the initial value of the state flow. This value is also used when the state flow
 * is reset using the SharingStarted. WhileSubscribed strategy with the replayExpirationMillis par
 */
public fun <T> Flow<T>.restartableStateIn(
  scope: CoroutineScope,
  started: SharingStarted,
  initialValue: T,
): RestartableStateFlow<T> {
  val sharingRestartable = SharingRestartableImpl(started)
  val stateFlow = stateIn(scope, sharingRestartable, initialValue)
  return object : RestartableStateFlow<T>, StateFlow<T> by stateFlow {
    override fun restart() = sharingRestartable.restart()
  }
}

/**
 * The internal implementation of the [SharingStarted], and [Restartable].
 */
private data class SharingRestartableImpl(
  private val sharingStarted: SharingStarted,
) : SharingStarted, Restartable {

  private val restartFlow = MutableSharedFlow<SharingCommand>(extraBufferCapacity = 2)

  override fun command(subscriptionCount: StateFlow<Int>): Flow<SharingCommand> {
    return merge(restartFlow, sharingStarted.command(subscriptionCount))
  }

  override fun restart() {
    restartFlow.tryEmit(SharingCommand.STOP_AND_RESET_REPLAY_CACHE)
    restartFlow.tryEmit(SharingCommand.START)
  }
}
