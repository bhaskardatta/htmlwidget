# HTML Widget for Smartspacer

This plugin allows you to display HTML content in a Smartspacer widget. You can either input HTML directly or provide a URL to load HTML content from.

## Features

- Display custom HTML content in a resizable widget
- Load HTML from a URL
- Configure widget dimensions
- Easy-to-use configuration interface

## Setup

1. Install the plugin
2. Add the HTML Widget to your Smartspacer setup
3. Configure the widget with your HTML content or URL
4. Adjust the size as needed

## Usage

### Direct HTML Input
You can paste HTML code directly into the configuration screen. The widget will render this HTML content within the constraints of the widget size.

### URL Mode
Alternatively, you can provide a URL to load HTML content from. The widget will fetch and display the content from this URL.

### Resizing
You can adjust the width and height of the widget to best fit your content and screen layout.

## Implementation Details

This plugin uses Android's WebView to render HTML content within a Smartspacer widget. It handles proper scaling and display of the content to fit within the widget boundaries.

## Requirements

- Smartspacer app installed
- Android 8.0 or higher

## Troubleshooting

If the widget doesn't display correctly:
- Check that your HTML is valid
- For URL mode, ensure the URL is accessible and returns valid HTML
- Try adjusting the widget size to better accommodate your content

## Privacy

This plugin only accesses the URLs you explicitly provide. HTML content entered directly is stored locally on your device.