"""
ryb_converter
Stefan, Yuzhao Heng
"""

import sys
from PyQt5.QtWidgets import QWidget, QApplication
from PyQt5.QtGui import QPainter, QColor, QBrush

def rgb_to_ryb(red, green, blue):
    """ Convert a red-green-blue system to a red-yellow-blue system. """
    tp = type(red)

    white = float(min(red, green, blue)) # Remove white
    red = float(red) - white
    green = float(green) - white
    blue = float(blue) - white

    mag_rgb = max(red, green, blue)

    yellow = min(red, green) # Yellow is mixed with red and green in RGB 
    red -= yellow
    green -= yellow
    
    if blue!=0 and green!=0: # So that doesn't exceed range 
        blue /= 2.0
        green /= 2.0
    
    yellow += green # Green is mixed with yellow and blue in RYB 
    blue += green

    mag_ryb = max(red, yellow, blue) # Normalize
    if mag_ryb!=0:
        ratio = mag_rgb/mag_ryb
        red *= ratio
        yellow *= ratio
        blue *= ratio
    
    red += white
    yellow += white
    blue += white
    return tp(red), tp(yellow), tp(blue)

def ryb_to_rgb(red, yellow, blue):
    """ Convert a red-yellow-blue system to a red-green-blue system. """
    tp = type(red)

    white = float(min(red, yellow, blue)) # Remove whie
    red = float(red) - white
    yellow = float(yellow) - white
    blue = float(blue) - white

    mag_ryb = max(red, yellow, blue)

    green = min(yellow, blue) # Green is mixed with yellow and blue in RYB 
    yellow -= green
    blue -= green

    if blue!=0 and green!=0: #So that doesn't exceed maximum range
        blue *= 2.0
        green *= 2.0
    
    red += yellow # Yellow is mixed with red and green in RGB 
    green += yellow

    mag_rgb = max(red, green, blue) # Normalize
    if mag_rgb:
        ratio = mag_ryb/mag_rgb
        red *= ratio
        green *= ratio
        blue *= ratio
    
    red += white
    green += white
    blue += white
    return tp(red), tp(green), tp(blue)

def complimentary(red, green, blue, limit=255):
    """ Return the complementary color values for a given color.  You must also give it the
    upper limit of the color values, typically 255 for GUIs, 1.0 for OpenGL. """
    return limit - red, limit - green, limit - blue

class ColorDisplay(QWidget):
    """ Test visually the color conversion result to RYB"""
    def __init__(self, colors, colors_text):
        super().__init__()
        self.left = 300
        self.right = 300
        self.width = 1440
        self.height = 810
        self.box_size = 180
        self.colors = colors
        self.colors_text = colors_text
        self.init_ui()
        self.qpainter = QPainter()
    def init_ui(self):
        """ Initilialize UI """
        self.setGeometry(self.left, self.right, self.width, self.height)
        self.setWindowTitle("RYB Color Test")
        self.show()
    def paintEvent(self, event):
        """ from PyQt, automatically called """
        self.qpainter = QPainter()
        self.qpainter.begin(self)
        for i in range(len(self.colors)):
            num_row = self.width / self.box_size
            x = (i % num_row) * self.box_size
            y = (i // num_row) * self.box_size
            self.draw_box(x, y, self.box_size, self.box_size, self.colors[i])
            self.draw_text(x, y, self.colors_text[i])
        self.qpainter.end()
    def draw_box(self, x, y, width, height, color):
        """ draw box by color """
        self.qpainter.setBrush(QBrush(QColor(color[0], color[1], color[2])))
        self.qpainter.drawRect(x, y, width, height)
    def draw_text(self, x, y, text):
        """ indicate the color of box """
        self.qpainter.drawText(x, y + 60, text)

# Debugging color code.  Not intended to be used as an application.
if __name__ == "__main__":
    RED = (255, 0, 0)
    GREEN = (0, 255, 0)
    BLUE = (0, 0, 255)
    CYAN = (0, 255, 255)
    MAGENTA = (255, 0, 255)
    YELLOW = (255, 255, 0)
    BLACK = (0, 0, 0)
    WHITE = (255, 255, 255)
    ORANGE = (255, 128, 0)
    DARK_GREEN = (0, 128, 0)
    SHALLOW_ORANGE = (255, 128, 64)
    BRIGHT_PINK = (255, 64, 128)
    BLUE_GREEN = (64, 255, 128)
    RED_GREEN = (128, 255, 64)
    GREEN_BLUE = (64, 128, 255)
    RED_BLUE = (128, 64, 255)
    RAND_TRIAL = (214, 51, 16)
    COLORS = [RED, GREEN, BLUE, CYAN, MAGENTA, YELLOW, BLACK, WHITE, ORANGE, DARK_GREEN,
              SHALLOW_ORANGE, BRIGHT_PINK, BLUE_GREEN, RED_GREEN, GREEN_BLUE, RED_BLUE,
              RAND_TRIAL]
    COLORS_TEXT = ['Red', 'Green', 'Blue', 'CYAN', 'Magenta', 'Yellow', 'Black', 'White',
                   'Orange', 'Dark Green', 'Shallow Orange', 'Bright Pink', 'Blue Green',
                   'Red Green', 'Green Blue', 'Red Blue', 'Rand Trial']
    COLORS_DOUBLE = []
    COLORS_TEXT_DOUBLE = []
    for i in range(len(COLORS)):
        color = COLORS[i]
        text = COLORS_TEXT[i]
        ryb = rgb_to_ryb(color[0], color[1], color[2])
        rgb = ryb_to_rgb(ryb[0], ryb[1], ryb[2])
        color_comp = complimentary(color[0], color[1], color[2])
        cryb = rgb_to_ryb(color_comp[0], color_comp[1], color_comp[2])
        crgb = ryb_to_rgb(cryb[0], cryb[1], cryb[2])

        COLORS_DOUBLE.append(color)
        COLORS_DOUBLE.append(color_comp)
        COLORS_TEXT_DOUBLE.append(text)
        COLORS_TEXT_DOUBLE.append(text + "_comp")

        print(text, 'RGB: ', color, 'RYB: ', ryb, "back to RGB: ", rgb)
        print(text, 'Comp RGB: ', color_comp, 'Comp RYB: ', cryb, "back to RGB: ", crgb)
        print()

        # print(text + ':', ryb) # For RYB visual testing
        # print(text + '_comp:', cryb)

    APP = QApplication(sys.argv)
    COLOR_DISPLAY = ColorDisplay(COLORS_DOUBLE, COLORS_TEXT_DOUBLE)
    sys.exit(APP.exec_())
