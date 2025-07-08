//package com.example.hitproduct.screen.dialog
//
//import android.app.AlertDialog
//import android.app.Dialog
//import android.os.Bundle
//import android.view.View
//import android.widget.Button
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.ProgressBar
//import android.widget.TextView
//import android.widget.Toast
//import androidx.fragment.app.DialogFragment
//import com.example.hitproduct.R
//import com.example.hitproduct.data.model.foodList
//
//
//class ShopDialogFragment : DialogFragment() {
//
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val view = requireActivity().layoutInflater
//            .inflate(R.layout.dialog_shop, null)
//        bindViews(view)
//        return AlertDialog.Builder(requireContext())
//            .setView(view)
//            .create()
//    }
//
//    private fun bindViews(root: View) {
//        val detailContainer = root.findViewById<LinearLayout>(R.id.detailContainer)
//            .apply { visibility = View.GONE }
//        val tvName   = root.findViewById<TextView>(R.id.tvFoodName)
//        val ivDetail = root.findViewById<ImageView>(R.id.ivFoodDetail)
//        val progHp   = root.findViewById<ProgressBar>(R.id.progFood1)
//        val progLove = root.findViewById<ProgressBar>(R.id.progFood2)
//        val btnBuy   = root.findViewById<Button>(R.id.btnBuyFood)
//
//        // dùng foodList từ model
//        foodList.forEach { food ->
//            root.findViewById<ImageView>(food.viewId)
//                .setOnClickListener {
//                    detailContainer.visibility = View.VISIBLE
//                    tvName.text       = food.name
//                    ivDetail.setImageResource(food.imgRes)
//                    progHp.progress   = food.hpGain
//                    progLove.progress = food.loveGain
//                    btnBuy.text       = "Buy ${food.price} 💰"
//                    btnBuy.setOnClickListener {
//                        Toast.makeText(requireContext(),
//                            "Mua thành công ${food.name} với giá ${food.price}",
//                            Toast.LENGTH_SHORT).show()
//                        // TODO: trừ tiền, update UI tiền
//                    }
//                }
//        }
//    }
//}
//}