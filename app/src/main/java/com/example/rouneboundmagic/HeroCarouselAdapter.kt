package com.example.rouneboundmagic

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HeroCarouselAdapter(
    private val heroes: List<HeroOption>,
    private val heroBitmaps: Map<HeroOption, Bitmap?>
) : RecyclerView.Adapter<HeroCarouselAdapter.HeroViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hero_card, parent, false)
        return HeroViewHolder(view)
    }

    override fun onBindViewHolder(holder: HeroViewHolder, position: Int) {
        val hero = heroes[position]
        holder.bind(hero, heroBitmaps[hero])
    }

    override fun getItemCount(): Int = heroes.size

    class HeroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val heroImage: ImageView = itemView.findViewById(R.id.heroCardImage)
        private val heroName: TextView = itemView.findViewById(R.id.heroCardName)
        private val heroDescription: TextView = itemView.findViewById(R.id.heroCardDescription)

        fun bind(hero: HeroOption, bitmap: Bitmap?) {
            heroName.setText(hero.displayNameRes)
            heroDescription.setText(hero.descriptionRes)
            if (bitmap != null) {
                heroImage.setImageBitmap(bitmap)
            } else {
                heroImage.setImageResource(R.drawable.ic_launcher_background)
            }
        }
    }
}
