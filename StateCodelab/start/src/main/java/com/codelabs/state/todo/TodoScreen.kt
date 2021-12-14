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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.codelabs.state.util.generateRandomTodoItem
import kotlin.random.Random


@Composable
fun TodoInputTextField(text: String, onTextChange: (String) -> Unit, modifier: Modifier) {
    // val (text, setText) = remember{ mutableStateOf("")}
    TodoInputText(text, onTextChange, modifier)
}

/**
 * This code adds a value and onValueChange parameter to TodoInputTextField. The value parameter is text, and the onValueChange parameter is onTextChange.
 */


/**
 * You declare a MutableState object in a composable three ways:

val state = remember { mutableStateOf(default) }
var value by remember { mutableStateOf(default) }
val (value, setValue) = remember { mutableStateOf(default) }

When creating State<T> (or other stateful objects) in composition, it's important to remember it. Otherwise it will be re-initialized every composition.

MutableState<T> similar to MutableLiveData<T>, but integrated with the compose runtime. Since it's observable, it will tell compose whenever it's updated so compose can recompose any composables that read it.
 */
@Composable
fun TodoItemEntryInput(onItemComplete: (TodoItem) -> Unit) {
    val (text, setText) = remember { mutableStateOf("") }

    val (icon, setIcon) = remember { mutableStateOf(TodoIcon.Default) }

    val iconsVisible = text.isNotBlank()

    /**
     * The value iconsVisible does not add a new state to TodoItemInput. There is no way for TodoItemInput to directly change it. Instead, it is based entirely upon the value of text. Whatever the value of text is in this recomposition, iconsVisible will be set accordingly and we can use it to show the correct UI.
     */

    val submit = {
        if(text.isNotBlank()) {
            onItemComplete(TodoItem(text, icon))
            setIcon(TodoIcon.Default)
            setText("")
        }
    }
    TodoItemInput(
        text = text,
        onTextChange = setText,
        icon = icon,
        onIconChange = setIcon,
        submit = submit,
        iconsVisible = iconsVisible
    ){
        TodoEditButton(
            onClick = submit,
            text = "Add",
            enabled = text.isNotBlank()
        )
    }
}

/**
 * When hoisting state, there are three rules to help you figure out where it should go

State should be hoisted to at least the lowest common parent of all composables that use the state (or read)
State should be hoisted to at least the highest level it may be changed (or modified)
If two states change in response to the same events they should be hoisted together
You can hoist state higher than these rules require, but underhoisting state will make it difficult or impossible to follow unidirectional data flow.
 */


/**
 * This transformation is a really important one to understand when using compose. We took a stateful composable, TodoItemInput, and split it into two composables. One with state (TodoItemEntryInput) and one stateless (TodoItemInput).
The stateless composable has all of our UI-related code, and the stateful composable doesn't have any UI-related code. By doing this, we make the UI code reusable in situations where we want to back the state differently
 */
@Composable
fun TodoItemInput(
    text: String,
    onTextChange: (String) -> Unit,
    icon: TodoIcon,
    onIconChange: (TodoIcon) -> Unit,
    submit: () -> Unit,
    iconsVisible: Boolean,
    buttonSlot: @Composable () -> Unit,
) {
    Column {
        Row(
            Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        ) {
            TodoInputText(
                text,
                onTextChange,
                Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                submit
            )

            // New code: Replace the call to TodoEditButton with the content of the slot

            Spacer(modifier = Modifier.width(8.dp))
            Box(Modifier.align(Alignment.CenterVertically)) { buttonSlot() }


        }
        if (iconsVisible) {
            AnimatedIconRow(icon, onIconChange, Modifier.padding(top = 8.dp))
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Stateless component that is responsible for the entire todo screen.
 *
 * @param items (state) list of [TodoItem] to display
 * @param onAddItem (event) request an item be added
 * @param onRemoveItem (event) request an item be removed
 */
@Composable
fun TodoScreen(
    items: List<TodoItem>,
    currentlyEditing: TodoItem?,
    onAddItem: (TodoItem) -> Unit,
    onRemoveItem: (TodoItem) -> Unit,
    onStartEdit: (TodoItem) -> Unit,
    onEditItemChange: (TodoItem) -> Unit,
    onEditDone: () -> Unit
) {
    Column {
        val enableTopSection = currentlyEditing == null

        TodoItemInputBackground(elevate = enableTopSection, modifier = Modifier.fillMaxWidth()) {
            if(enableTopSection){
                TodoItemEntryInput(onItemComplete = onAddItem)
            }else{
                Text(
                    "Editing item",
                    style = MaterialTheme.typography.h6,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(16.dp)
                        .fillMaxWidth()
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(top = 8.dp)
        ) {
            items(items = items) { todo ->

                if (currentlyEditing?.id == todo.id) {
                    TodoItemInlineEditor(
                        item = currentlyEditing,
                        onEditItemChange = onEditItemChange,
                        onEditDone = { onEditDone() },
                        onRemoveItem = { onRemoveItem(todo) })
                } else {
                    TodoRow(
                        todo = todo,
                        onItemClicked = { onStartEdit(it) },
                        modifier = Modifier.fillParentMaxWidth()
                    )
                }
            }
        }

        // For quick testing, a random item generator button
        Button(
            onClick = { onAddItem(generateRandomTodoItem()) },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
        ) {
            Text("Add random item")
        }
    }
}

/**
 * Stateless composable that displays a full-width [TodoItem].
 *
 * @param todo item to show
 * @param onItemClicked (event) notify caller that the row was clicked
 * @param modifier modifier for this element
 *
 * When adding memory to a composable, always ask yourself "will some caller reasonably want to control this?"

If the answer is yes, make a parameter instead.

If the answer is no, keep it as a local variable.
 */
@Composable
fun TodoRow(
    todo: TodoItem,
    onItemClicked: (TodoItem) -> Unit, modifier: Modifier = Modifier,
    iconAlpha: Float = remember(todo.id) { randomTint() }
) {
    Row(
        modifier = modifier
            .clickable { onItemClicked(todo) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(todo.task)

        /**
         * remember gives a composable function memory.

        A value computed by remember will be stored in the composition tree, and only be recomputed if the keys to remember change.

        You can think of remember as giving storage for a single object to a function the same way a private val property does in an object.

        key arguments – the "key" that this remember uses, this is the part that is passed in parenthesis. Here we're passing todo.id as the key.
        calculation – a lambda that computes a new value to be remembered, passed in a trailing lambda. Here we're computing a random value with randomTint().

         */

        /**
         * What is LocalContentColor.current?

        LocalContentColor gives you the preferred color for content such as Icons and Typography. It is changed by composables such as Surface that draw a background.
         */
        Icon(
            imageVector = todo.icon.imageVector,
            tint = LocalContentColor.current.copy(alpha = iconAlpha),
            contentDescription = stringResource(id = todo.icon.contentDescription)
        )

        /**
         * Recomposition is the process of running the same composables again to update the tree when their data changes

        The reason the icons update every time the TodoRow recompose is because TodoRow has a hidden side-effect. A side-effect is any changes that's visible outside of the execution of a composable function.

        The call to Random.nextFloat() updates the internal random variable used in a pseudo-random number generator. This is how Random returns a different value every time you ask for a random number.

        A side-effect is any change that's visible outside of a composable function.

        Recomposing a composable should be side-effect free.

        For example, updating state in a ViewModel, calling Random.nextInt(), or writing to a database are all side-effects.


         */


    }
}

@Composable
fun TodoItemInlineEditor(
    item: TodoItem,
    onEditItemChange: (TodoItem) -> Unit,
    onEditDone: () -> Unit,
    onRemoveItem: () -> Unit
) = TodoItemInput(
    text = item.task,
    onTextChange = { onEditItemChange(item.copy(task = it)) },
    icon = item.icon,
    onIconChange = { onEditItemChange(item.copy(icon = it)) },
    submit = onEditDone,
    iconsVisible = true,
    buttonSlot = {
        Row {
            val shrinkButtons = Modifier.widthIn(20.dp)
            TextButton(onClick = onEditDone, modifier = shrinkButtons) {
                Text(
                    text = "\uD83D\uDCBE", // floppy disk
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(30.dp)
                )
            }
            TextButton(onClick = onRemoveItem, modifier = shrinkButtons) {
                Text(
                    text = "❌",
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(30.dp)
                )
            }
        }
    }
)

private fun randomTint(): Float {
    return Random.nextFloat().coerceIn(0.3f, 0.9f)
}

@Preview
@Composable
fun PreviewTodoItemInput() = TodoItemEntryInput(onItemComplete = {})

@Preview
@Composable
fun PreviewTodoScreen() {
    val items = listOf(
        TodoItem("Learn compose", TodoIcon.Event),
        TodoItem("Take the codelab"),
        TodoItem("Apply state", TodoIcon.Done),
        TodoItem("Build dynamic UIs", TodoIcon.Square)
    )
    TodoScreen(items, null, {}, {}, {}, {}, {})
}

@Preview
@Composable
fun PreviewTodoRow() {
    val todo = remember { generateRandomTodoItem() }
    TodoRow(todo = todo, onItemClicked = {}, modifier = Modifier.fillMaxWidth())
}
