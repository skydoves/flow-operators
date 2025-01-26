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
package com.skydoves.flow.operators.pausable

import kotlinx.coroutines.flow.StateFlow

/**
 * [PausableStateFlow] extends both [StateFlow] and [PausableStateFlow], and is designed to
 * pause and resume the emission of the upstream flow. It functions just like a regular [StateFlow],
 * but with the added ability to pause and resume the upstream emission when needed.
 */
public interface PausableStateFlow<out T> : StateFlow<T>, Pausable {
  /**
   * The representation of the [Pausable] object that should be able to pause an action.
   */
  override fun pause()

  /**
   * The representation of the [Pausable] object that should be able to resume an action.
   */
  override fun resume()
}
