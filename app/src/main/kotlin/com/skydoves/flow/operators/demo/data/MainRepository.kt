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
package com.skydoves.flow.operators.demo.data

import android.util.Log
import com.skydoves.flow.operators.demo.network.DisneyService
import com.skydoves.flow.operators.demo.network.Poster
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MainRepository(private val disneyService: DisneyService) {

  fun fetchPostersFlow(): Flow<ApiResponse<List<Poster>>> = flow {
    Log.d("MainRepository", "fetchPostersFlow")
    val response = disneyService.fetchDisneyPosters()
    emit(response)
  }
}
