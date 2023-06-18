package mb.safeEat.services.api.endpoints

import mb.safeEat.services.api.models.RestaurantSection
import retrofit2.http.*

sealed interface RestaurantSectionEndpoint {
    @GET
    fun findAll(): List<RestaurantSection>

    @GET("/{id}")
    fun findById(@Path("id") id: String?): RestaurantSection

    @POST
    fun create(@Body restaurantSection: RestaurantSection?): RestaurantSection

    @POST("/many")
    fun createMany(@Body restaurantSections: List<RestaurantSection?>?): List<RestaurantSection>

    @PUT
    fun update(@Body restaurantSection: RestaurantSection?): RestaurantSection

    @DELETE("/{id}")
    fun delete(@Path("id") id: String?)
}
