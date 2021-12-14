/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codelabs.state.todo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
class TodoViewModel : ViewModel() {

/*
    private var _todoItems = MutableLiveData(listOf<TodoItem>())
    val todoItems: LiveData<List<TodoItem>> = _todoItems
*/

    private var currentEditPosition by mutableStateOf(-1)

    // state: todoItems
    var todoItems = mutableStateListOf<TodoItem>()
        private set

    //state
    val currentEditItem : TodoItem? get() = todoItems.getOrNull(currentEditPosition)

    /**
     * Whenever a composable calls currentEditItem, it will observe changes to both todoItems and currentEditPosition. If either change, the composable will call the getter again to get the new value.
     */

    /**
     * mutableStateListOf allows us to create an instance of MutableList that is observable. This means that we can work with todoItems in the same way we work with a MutableList, removing the overhead of working with LiveData<List>.
     */

    fun addItem(item: TodoItem) {
        //todoItems.value = _todoItems.value!! + listOf(item)
        todoItems.add(item)
    }

    fun removeItem(item: TodoItem) {
       /* _todoItems.value = _todoItems.value!!.toMutableList().also {
            it.remove(item)
        }*/

        todoItems.remove(item)
        onEditDone()
    }

    fun onEditItemSelected(item: TodoItem){
        currentEditPosition = todoItems.indexOf(item)
    }

    fun onEditDone(){
        currentEditPosition = -1
    }

    fun onEditItemChange(item: TodoItem){
        val currentItem = requireNotNull(currentEditItem)
        require(currentItem.id == item.id){
            "You can only change an item with the same id as currentEditItem"
        }

        todoItems[currentEditPosition] = item
    }
}
