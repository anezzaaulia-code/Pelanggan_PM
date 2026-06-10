package anezza.aulia.pelanggan_pm.helper

object ApiConfig {
    const val BASE_URL = "http://192.168.1.143:8000/api"

    const val HEALTH = "$BASE_URL/health"

    const val LOGIN = "$BASE_URL/auth/login"
    const val REGISTER = "$BASE_URL/auth/register"
    const val LOGOUT = "$BASE_URL/auth/logout"

    const val ME = "$BASE_URL/me"
    const val PROFILE = "$BASE_URL/profile"
    const val PASSWORD = "$BASE_URL/password"

    const val STORE = "$BASE_URL/store"
    const val BANNERS = "$BASE_URL/banners"

    const val PRODUCTS = "$BASE_URL/products"
    const val ADDRESSES = "$BASE_URL/addresses"
    const val ORDERS = "$BASE_URL/orders"
    const val REVIEWS = "$BASE_URL/reviews"
    const val MY_REVIEWS = "$BASE_URL/reviews/me"
}