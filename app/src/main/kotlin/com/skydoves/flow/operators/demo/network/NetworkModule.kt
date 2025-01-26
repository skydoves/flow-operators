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
package com.skydoves.flow.operators.demo.network

import com.skydoves.sandwich.retrofit.adapters.ApiResponseCallAdapterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object NetworkModule {

  private val okHttpClient: OkHttpClient =
    OkHttpClient.Builder().build()

  private val retrofit: Retrofit = Retrofit.Builder()
    .client(okHttpClient)
    .baseUrl(
      "https://gist.githubusercontent.com/skydoves/aa3bbbf495b0fa91db8a9e89f34e4873/" +
        "raw/a1a13d37027e8920412da5f00f6a89c5a3dbfb9a/",
    )
    .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
    .addCallAdapterFactory(ApiResponseCallAdapterFactory.create())
    .build()

  val disneyService: DisneyService = retrofit.create(DisneyService::class.java)
}
