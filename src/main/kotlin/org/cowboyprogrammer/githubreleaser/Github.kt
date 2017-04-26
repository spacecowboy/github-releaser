package org.cowboyprogrammer.githubreleaser

import okhttp3.Cache
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.io.File


object Credentials {
    var user: String = ""
    var repo: String = ""
    var token: String = ""
}

private val API_URL = "https://api.github.com/"
private val githubApi: GithubApi by lazy {

    val cacheDir = File(".okhttpcache")
    if (!cacheDir.isDirectory && !cacheDir.mkdir()) {
        throw RuntimeException("Failed to create cache directory: ${cacheDir.canonicalPath}")
    }
    val cache = Cache(cacheDir, 1024 * 1024 * 10)

    val httpBuilder = OkHttpClient.Builder().cache(cache)

    if (Credentials.token.isNotEmpty()) {
        httpBuilder.addInterceptor {
            val original = it.request()

            val request = original.newBuilder()
                    .addHeader("Authorization", "token ${Credentials.token}")
                    .method(original.method(), original.body())
                    .build()

            it.proceed(request)
        }
    }

    val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(API_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(httpBuilder.build())
            .build()
    retrofit.create(GithubApi::class.java)
}

interface GithubApi {
    @GET("repos/{user}/{repo}/releases/tags/{tag}")
    fun getReleaseByTag(@Path("user") user: String,
                        @Path("repo") repo: String,
                        @Path("tag") tag: String): Call<Release>

    @POST("repos/{user}/{repo}/releases")
    fun createRelease(@Path("user") user: String,
                      @Path("repo") repo: String,
                      @Body tagRelease: TagRelease): Call<Release>


    @POST
    fun uploadReleaseAsset(@Url url: String,
                           @Header("Content-Type") contentType: String,
                           @Query("name") name: String,
                           @Body asset: RequestBody): Call<UploadResult>
}

data class TagRelease(val tag_name: String, val name: String = tag_name)

data class Release(val id: Long, val tag_name: String, val upload_url: String) {
    val upload_url_sane: String
        get() {
            return upload_url.substringBefore("{")
        }
}

data class UploadResult(val name: String, val label: String, val content_type: String,
                        val size: Long, val browser_download_url: String)

fun getReleaseByTag(tag: String): Response<Release> =
        githubApi.getReleaseByTag(user = Credentials.user, repo = Credentials.repo, tag = tag).execute()

fun createRelease(tag: String): Response<Release> =
        githubApi.createRelease(user = Credentials.user, repo = Credentials.repo, tagRelease = TagRelease(tag)).execute()

fun uploadReleaseFile(url: String, file: File): Response<UploadResult> {
    return githubApi.uploadReleaseAsset(url = url,
            contentType = file.contentType,
            name = file.name,
            asset = RequestBody.create(MediaType.parse(file.contentType), file)).execute()
}

fun getOrCreateRelease(tag: String): Release {
    val response = getReleaseByTag(tag)

    if (response.isSuccessful) {
        return response.body()
    }

    when (response.code()) {
        404 -> {
            // Create release because no such release exists
            @Suppress("NAME_SHADOWING")
            val response = createRelease(tag)

            if (response.isSuccessful) {
                return response.body()
            }

            throw RuntimeException("Error: Could not create a release for tag '$tag', response: \n${response.errorBody().string()}")
        }
        else -> {
            throw RuntimeException("Error: Could not find a release for tag '$tag', response: \n${response.errorBody().string()}")
        }
    }
}
