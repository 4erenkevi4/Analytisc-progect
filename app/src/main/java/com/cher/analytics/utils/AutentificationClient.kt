package com.cher.analytics.utils

import android.content.Context
import android.os.Environment
import com.github.instagram4j.instagram4j.IGClient
import com.github.instagram4j.instagram4j.exceptions.IGLoginException
import com.github.instagram4j.instagram4j.exceptions.IGResponseException
import com.github.instagram4j.instagram4j.responses.accounts.LoginResponse
import com.github.instagram4j.instagram4j.utils.IGUtils
import okhttp3.OkHttpClient.Builder
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException


class AutentificationClient (){

    companion object {
        fun getDir(context : Context): String =  "${context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)}"
        @Throws(
            IGLoginException::class,
            IGResponseException::class,
            ClassNotFoundException::class,
            FileNotFoundException::class,
            IOException::class
        )
        fun serializeLogin(username: String?, password: String?,context : Context ) {
            val directory = File(getDir(context))
            directory.mkdirs()
            val to = File(directory, "clien.ser")
            val cookFile = File(directory, "cooki.ser")
            val lib = IGClient.Builder().username(username).password(password)
                .onLogin { cli: IGClient?, lr: LoginResponse ->
                    println(lr.feedback_message)
                }
                .login()
            println("Serializing. . .")
            lib.serialize(to, cookFile)
            println("Deserializing. . .")
            val saved = IGClient.deserialize(to, cookFile, formTestHttpClientBuilder())
            println(lib.toString())
            println(saved.toString())
        }

        @Throws(
            IGLoginException::class,
            IGResponseException::class,
            ClassNotFoundException::class,
            FileNotFoundException::class,
            IOException::class
        )
        fun getClientFromSerialize(client: String, cookie: String, context : Context): IGClient? {
            val directory = File(getDir(context))
            directory.mkdirs()
            val to = File("$directory/$client")
            val cookFile = File("$directory/$cookie")
            return try {
                IGClient.deserialize(to, cookFile, formTestHttpClientBuilder())
            } catch (
                e: Exception
            ) {
                null
            }
        }

        // logging interceptor
        private val loggingInterceptor = HttpLoggingInterceptor { msg: String? -> }.setLevel(
            HttpLoggingInterceptor.Level.BODY
        )

        fun formTestHttpClientBuilder(): Builder {
            return IGUtils.defaultHttpClientBuilder().addInterceptor(loggingInterceptor)
        }
    }
}