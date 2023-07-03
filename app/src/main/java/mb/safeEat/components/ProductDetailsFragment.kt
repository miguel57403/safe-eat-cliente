package mb.safeEat.components

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import mb.safeEat.R
import mb.safeEat.extensions.Alertable
import mb.safeEat.functions.suspendToLiveData
import mb.safeEat.services.api.api
import mb.safeEat.services.api.models.Ingredient

class ProductDetailsFragment(private val navigation: NavigationListener) : Fragment(), Alertable {
    private lateinit var items: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_product_details, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initHeader(view)
        initAdapter(view)
        initScreenEvents(view)
        loadInitialData(view)
    }

    private fun initHeader(view: View) {
        val title = view.findViewById<TextView>(R.id.header_title)
        val backButton = view.findViewById<MaterialCardView>(R.id.header_back_button)
        title.text = resources.getString(R.string.t_product_details)
        backButton.setOnClickListener { navigation.onBackPressed() }
    }

    private fun initAdapter(view: View) {
        items = view.findViewById(R.id.product_detail_items)
        items.layoutManager = LinearLayoutManager(view.context)
        items.adapter = ProductDetailAdapter()
    }

    private fun initScreenEvents(view: View) {
        val addToCartButton = view.findViewById<Button>(R.id.product_detail_button)
        addToCartButton.setOnClickListener {
            val dialog = ProductAddedDialog()
            dialog.show(navigation.getSupportFragmentManager(), dialog.tag)
            dialog.lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    // TODO: Try remove this observer
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        navigation.navigateTo(CartInitialFragment(navigation))
                    }
                }
            })
        }
    }

    private fun loadInitialData(view: View) {
        // TODO: Remove hardcode
        val productId = "649f54ad6665ea2c2dede4ee"

        loadProductData(view, productId)
        loadIngredientsData(view, productId)
    }

    private fun loadProductData(view: View, productId: String) {
        suspendToLiveData { api.products.findById(productId) }.observe(viewLifecycleOwner) { result ->
            result.fold(onSuccess = { product ->
                val name = view.findViewById<TextView>(R.id.product_details_content_card_title)
                val price = view.findViewById<TextView>(R.id.product_details_content_card_price)

                name.text = product.name!!
                price.text = product.price.toString()

            }, onFailure = {
                alertThrowable(it)
            })
        }
    }

    private fun loadIngredientsData(view: View, productId: String) {
        suspendToLiveData { api.ingredients.findByAllProduct(productId) }.observe(viewLifecycleOwner) { result ->
            result.fold(onSuccess = { ingredients ->
                val initData = mapInitialData(ingredients)
                (items.adapter as ProductDetailAdapter).loadInitialData(initData)

                val alert = view.findViewById<MaterialCardView>(R.id.product_details_content_alert)
                alert.isVisible = ingredients.any { it.isRestricted!! }
            }, onFailure = {
                alertThrowable(it)
            })
        }
    }

    private fun mapInitialData(ingredients: List<Ingredient>): ArrayList<ProductDetail> {
        return ingredients.map { ingredient ->
            ProductDetail(ingredient.name!!, ingredient.isRestricted!!)
        }.toCollection(ArrayList())
    }

    private fun createList(): java.util.ArrayList<ProductDetail> {
        return arrayListOf(
            ProductDetail("Carne moida bovina", false),
            ProductDetail("Pimenta", true),
        )
    }
}


class ProductDetailAdapter : RecyclerView.Adapter<ProductDetailAdapter.ViewHolder>() {
    private var data = ArrayList<ProductDetail>()

    @SuppressLint("NotifyDataSetChanged")
    fun loadInitialData(newData: ArrayList<ProductDetail>) {
        data = newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_product_detail, parent, false)
    )

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(data[position])

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val content = itemView.findViewById<TextView>(R.id.product_details_item_content)
        private val image = itemView.findViewById<ImageView>(R.id.product_details_item_image)

        fun bind(item: ProductDetail) {
            content.text = item.name
            if (item.isRestrict) {
                val color = ContextCompat.getColor(itemView.context, R.color.red_500)
                content.setTextColor(color)
                image.setColorFilter(color)
            }
        }
    }
}

data class ProductDetail(
    val name: String,
    val isRestrict: Boolean,
)
