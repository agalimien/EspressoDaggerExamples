package com.codingwithmitch.espressodaggerexamples.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.codingwithmitch.espressodaggerexamples.BaseApplication
import com.codingwithmitch.espressodaggerexamples.R
import com.codingwithmitch.espressodaggerexamples.models.Category
import com.codingwithmitch.espressodaggerexamples.ui.viewmodel.*
import com.codingwithmitch.espressodaggerexamples.ui.viewmodel.state.MAIN_VIEW_STATE_BUNDLE_KEY
import com.codingwithmitch.espressodaggerexamples.ui.viewmodel.state.MainStateEvent.*
import com.codingwithmitch.espressodaggerexamples.ui.viewmodel.state.MainViewState
import com.codingwithmitch.espressodaggerexamples.util.printLogD
import com.codingwithmitch.espressodaggerexamples.viewmodels.MainViewModelFactory
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class MainActivity : AppCompatActivity(),
    UICommunicationListener
{

    private val CLASS_NAME = "MainActivity"

    @Inject
    lateinit var viewModelFactory: MainViewModelFactory

    val viewModel: MainViewModel by viewModels {
        viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as BaseApplication)
            .appComponent
            .inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupActionBar()

        subscribeObservers()

        restoreInstanceState(savedInstanceState)
    }

    private fun restoreInstanceState(savedInstanceState: Bundle?){
        savedInstanceState?.let { inState ->
            (inState[MAIN_VIEW_STATE_BUNDLE_KEY] as MainViewState?)?.let { viewState ->
                viewModel.setViewState(viewState)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        viewModel.clearActiveJobCounter()
        outState.putParcelable(
            MAIN_VIEW_STATE_BUNDLE_KEY,
            viewModel.getCurrentViewStateOrNew()
            )
        super.onSaveInstanceState(outState)
    }

    private fun subscribeObservers(){
        viewModel.viewState.observe(this, Observer { viewState ->
            if(viewState != null){

                displayMainProgressBar(viewModel.areAnyJobsActive())

                viewState.errorMessage?.getContentIfNotHandled()?.let { message ->
                    displaySnackbar(message, Snackbar.LENGTH_SHORT)
                }
            }
        })
    }

    override fun displayToastMessage(message: String, length: Int) {
        Toast.makeText(this, message, length).show()
    }

    override fun displaySnackbar(message: String, length: Int) {
        Snackbar.make(this.window.decorView, message, length).show()
    }

    override fun displayMainProgressBar(isLoading: Boolean){
        if(isLoading){
            main_progress_bar.visibility = View.VISIBLE
        }
        else{
            main_progress_bar.visibility = View.GONE
        }
    }

    private fun setupActionBar() {
        tool_bar.setupWithNavController(
            findNavController(R.id.nav_host_fragment)
        )
    }

    override fun showCategoriesMenu(categories: ArrayList<Category>) {
        printLogD(CLASS_NAME, "showCategoriesMenu")
        val menu = tool_bar.menu
        menu.clear()
        categories.add(Category(MENU_ITEM_ID_GET_ALL_BLOGS, MENU_ITEM_NAME_GET_ALL_BLOGS))
        for((index, category) in categories.withIndex()){
            menu.add(0, category.pk , index, category.category_name)
        }
        tool_bar.invalidate()
        tool_bar.setOnMenuItemClickListener { menuItem ->
           onMenuItemSelected(categories, menuItem)
        }
    }

    private fun onMenuItemSelected(categories: List<Category>, menuItem: MenuItem): Boolean{
        for(category in categories){
            if(category.pk == menuItem.itemId){
                viewModel.clearLayoutManagerState()
                if(category.category_name.equals(MENU_ITEM_NAME_GET_ALL_BLOGS)){
                    viewModel.setStateEvent(GetAllBlogs())
                }else{
                    viewModel.setStateEvent(SearchBlogsByCategory(category.category_name))
                }
                return true
            }
        }
        return false
    }

    override fun hideCategoriesMenu() {
        printLogD(CLASS_NAME, "hideCategoriesMenu")
        tool_bar.menu.clear()
        tool_bar.invalidate()
    }

    override fun hideToolbar() {
        tool_bar.visibility = View.GONE
    }

    override fun showToolbar() {
        tool_bar.visibility = View.VISIBLE
    }

    override fun hideStatusBar() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        hideToolbar()
    }

    override fun showStatusBar() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        showToolbar()
    }

    override fun expandAppBar() {
        findViewById<AppBarLayout>(R.id.app_bar).setExpanded(true)
    }

    companion object {

        const val MENU_ITEM_ID_GET_ALL_BLOGS = 99999999
        const val MENU_ITEM_NAME_GET_ALL_BLOGS = "All"
    }
}


















