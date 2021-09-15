package com.shortcut.explorer.data.network

import com.shortcut.explorer.data.network.model.OnFail
import com.shortcut.explorer.data.network.model.OnSuccess
import com.shortcut.explorer.data.network.model.Resource
import com.shortcut.explorer.data.network.model.ServerResponse
import kotlinx.coroutines.yield
import org.json.JSONException
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Wraps network call and return corresponding [Resource] depending on final requested response.
 * An error [Resource] is generated by [onFailed] or ongoing try-catch if the network request is
 * failed.
 */
class NetworkWrapper{

    /**
     * Triggers the network transaction. [call] lambda contains the network call and will return
     * network result.
     *
     * @param T : request type.
     * @param call : inline function, containing the network executable.
     */
    private suspend fun <T> run(call: suspend () -> Response<ServerResponse<T>>) = call.invoke()

    /**
     * Jus start the transaction with provided [call] object.
     *
     * @param T :Expecting result type
     * @param call : inline function, containing the network executable.
     * @return : returns a network result wraped with [Resource]. Inner data is a [ServerResponse].
     */
    suspend fun <T> fetch (call:suspend () -> Response<ServerResponse<T>>) = fetch(call,{})

    /**
     * Jus start the transaction with provided [call] object and invoke [onSuccess] lambda if
     * the call goes successfully.
     *
     * @param T :Expecting result type
     * @param call : inline function, containing the network executable.
     * @param onSuccess : inline function, to be invoked if the call returns successfully.
     * @return : returns a network result wraped with [Resource]. Inner data is a [ServerResponse].
     */
    suspend fun <T> fetch (call:suspend () -> Response<ServerResponse<T>>,
                           onSuccess: OnSuccess<T>
    ) = fetch(call,onSuccess,{_,_->})

    /**
     * Jus start the transaction with provided [call] object. Invokes [onSuccess] lambda if
     * the call goes successfully and [onFail] otherwise.
     *
     * @param T :Expecting result type
     * @param call : inline function, containing the network executable.
     * @param onSuccess : inline function, to be invoked if the call returns successfully.
     * @param onFail : inline function, to be invoked if the call fails.
     * @return : returns a network result wraped with [Resource]. Inner data is a [ServerResponse].
     */
    suspend fun <T> fetch (call: suspend () -> Response<ServerResponse<T>>,
                           onSuccess: OnSuccess<T>,
                           onFail: OnFail
    ) : Resource<ServerResponse<T>> {
        /**
         * yield()
         * Cancel the job if request was cancelled already
         */
        yield()

        return try {
            val result = run(call)
            yield()

            if (result.isSuccessful)
                onSucceed(result,onSuccess)

            else
                onFailed(result.code(), onFail)

        } catch (e: HttpException) {
            e.printStackTrace()
            Resource.error(errorCode = e.code())

        } catch (e: ConnectException) {
            e.printStackTrace()
            Resource.error(errorCode = CONNECT_EXCEPTION)

        } catch (e: SocketTimeoutException) {
            e.printStackTrace()
            Resource.error(errorCode =  SOCKET_TIME_OUT_EXCEPTION)

        } catch (e: UnknownHostException) {
            e.printStackTrace()
            Resource.error(errorCode = UNKNOWN_HOST_EXCEPTION)

        } catch (e: Exception) {
            e.printStackTrace()
            Resource.error(errorCode = EXCEPTION)
        }
    }

    /**
     * Manage anything that should be done hen the result is successful.
     *
     * @param T : data type.
     * @param body : main content that received.
     * @param onSuccess : passed callback function to invoke.
     * @return : a [Resource] of type [Status.SUCCESS].
     */
    private suspend fun <T> onSucceed(body: Response<ServerResponse<T>>, onSuccess: OnSuccess<T>): Resource<ServerResponse<T>>{
        onSuccess(body.body())
        return Resource.success(body.body())
    }

    /**
     * Extracts error message out of the error body. Since, we have no clear vision of the possible
     * error bodies, here we pass a sample string "msg" as extracted content.
     *
     * @param T : data type.
     * @param code : error code.
     * @param onFail : passed callback function to be invoked on transaction failure.
     * @return : a [Resource] of type [Status.ERROR].
     */
    private suspend fun <T> onFailed(code: Int, onFail: OnFail): Resource<ServerResponse<T>>{
        return try {
            onFail.invoke("msg",code)
            Resource.error( "msg", errorCode = code)

        }catch (exception: JSONException){
            Resource.error(exception.localizedMessage)
        }
    }

    companion object {
        const val CONNECT_EXCEPTION = 600           /** It means there is no internet connection around. */
        const val SOCKET_TIME_OUT_EXCEPTION = 601   /** No internet connection or poor connection quality */
        const val UNKNOWN_HOST_EXCEPTION = 602      /** Probably a dns issue or wrong host address */
        const val EXCEPTION = 603                   /** Something that should be investigated */
    }
}
