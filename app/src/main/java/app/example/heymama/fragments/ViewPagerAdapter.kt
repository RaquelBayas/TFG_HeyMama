package app.example.heymama.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerAdapter(supportFragmentManager: FragmentManager) : FragmentPagerAdapter(supportFragmentManager,
BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private var fragmentList = arrayListOf<Fragment>()
    private var fragmentTitleList = arrayListOf<String>()

    /**
     * Devuelve la cantidad de fragmentos de la lista
     */
    override fun getCount(): Int {
       return fragmentList.size
    }

    /**
     * Devuelve el fragmento de la posición solicitada
     * @param position Int
     */
    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    /**
     * Devuelve el título correspondiente al fragmento solicitado
     * @param position Int
     */
    override fun getPageTitle(position: Int): CharSequence? {
        return fragmentTitleList[position]
    }

    /**
     * Añade el fragment y su título
     * @param fragment Fragment
     * @param title String
     */
     fun addFragment(fragment: Fragment, title: String) {
        fragmentList.add(fragment)
        fragmentTitleList.add(title)
    }
}