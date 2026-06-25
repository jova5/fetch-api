package ba.fluxor.fetchapi.component

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactInput(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  placeholder: String = "",
  enabled: Boolean = true,
  singleLine: Boolean = true,
) {
  if (singleLine) {
    SingleLineCompactInput(value, onValueChange, modifier, placeholder, enabled)
  } else {
    MultilineCompactInput(value, onValueChange, modifier, placeholder, enabled)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SingleLineCompactInput(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier,
  placeholder: String,
  enabled: Boolean,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isHovered by interactionSource.collectIsHoveredAsState()

  val textColor = if (enabled) {
    MaterialTheme.colorScheme.onSurface
  } else {
    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
  }

  BasicTextField(
    value = value,
    onValueChange = onValueChange,
    enabled = enabled,
    modifier = modifier
      .height(32.dp)
      .hoverable(interactionSource, enabled)
      .pointerHoverIcon(
        icon = if (enabled) PointerIcon.Text else PointerIcon.Default,
        overrideDescendants = true
      ),
    singleLine = true,
    interactionSource = interactionSource,
    textStyle = LocalTextStyle.current.copy(
      fontSize = LocalTextStyle.current.fontSize * 0.9f,
      color = MaterialTheme.colorScheme.onSurface
    ),
    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
    decorationBox = { innerTextField ->
      OutlinedTextFieldDefaults.DecorationBox(
        value = value,
        innerTextField = innerTextField,
        enabled = enabled,
        singleLine = true,
        visualTransformation = VisualTransformation.None,
        interactionSource = interactionSource,
        placeholder = {
          Text(
            text = placeholder,
            fontSize = LocalTextStyle.current.fontSize * 0.9f,
            color = textColor
          )
        },
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
        container = {
          OutlinedTextFieldDefaults.Container(
            enabled = enabled,
            isError = false,
            interactionSource = interactionSource,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MaterialTheme.colorScheme.primary,
              unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
              focusedContainerColor = Color.Transparent,
              unfocusedContainerColor = if (isHovered)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
              else Color.Transparent
            ),
            focusedBorderThickness = 1.5.dp,
            unfocusedBorderThickness = 0.5.dp,
          )
        }
      )
    }
  )
}

@Composable
private fun MultilineCompactInput(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier,
  placeholder: String,
  enabled: Boolean,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isHovered by interactionSource.collectIsHoveredAsState()
  val isFocused by interactionSource.collectIsFocusedAsState()

  val hScroll = rememberScrollState()
  val vScroll = rememberScrollState()
  val shape = RoundedCornerShape(4.dp)

  val textColor = if (enabled) {
    MaterialTheme.colorScheme.onSurface
  } else {
    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
  }

  val borderColor = if (isFocused) {
    MaterialTheme.colorScheme.primary
  } else {
    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
  }
  val borderWidth = if (isFocused) 1.5.dp else 0.5.dp
  val containerColor = if (!isFocused && isHovered) {
    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
  } else {
    Color.Transparent
  }

  BasicTextField(
    value = value,
    onValueChange = onValueChange,
    enabled = enabled,
    modifier = modifier
      .fillMaxWidth()
      .height(32.dp)
      .hoverable(interactionSource, enabled)
      .pointerHoverIcon(
        icon = if (enabled) PointerIcon.Text else PointerIcon.Default,
        overrideDescendants = true
      ),
    singleLine = false,
    interactionSource = interactionSource,
    textStyle = LocalTextStyle.current.copy(
      fontSize = LocalTextStyle.current.fontSize * 0.9f,
      color = MaterialTheme.colorScheme.onSurface
    ),
    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
    decorationBox = { innerTextField ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .clip(shape)
          .background(containerColor)
          .border(borderWidth, borderColor, shape),
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(hScroll)
            .verticalScroll(vScroll)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
          if (value.isEmpty()) {
            Text(
              text = placeholder,
              fontSize = LocalTextStyle.current.fontSize * 0.9f,
              color = textColor,
              maxLines = 1,
            )
          }
          innerTextField()
        }
      }
    }
  )
}
