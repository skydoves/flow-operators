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
package com.skydoves.flow.operators.demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skydoves.flow.operators.demo.data.MainRepository
import com.skydoves.flow.operators.demo.network.Poster
import com.skydoves.flow.operators.restartable.RestartableStateFlow
import com.skydoves.flow.operators.restartable.restartableStateIn
import com.skydoves.sandwich.getOrThrow
import com.skydoves.sandwich.isSuccess
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest

class MainViewModel(mainRepository: MainRepository) : ViewModel() {

  @OptIn(ExperimentalCoroutinesApi::class)
  private val restartablePoster: RestartableStateFlow<List<Poster>> =
    mainRepository.fetchPostersFlow()
      .filter { it.isSuccess }
      .mapLatest { it.getOrThrow() }
      .restartableStateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
      )

  val posters: StateFlow<List<Poster>> = restartablePoster

  fun restart() {
    restartablePoster.restart()
  }
}
