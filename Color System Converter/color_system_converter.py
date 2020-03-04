from PIL import Image
import numpy as np

import ryb_converter
import image_factory

COLOR_DEPTH = 8
I_RED = 0
I_YELLOW = I_GREEN = 1
I_BLUE = 2
NUM_CHANNEL = 3
TAGS_CHANNEL = {I_RED: "R", I_YELLOW: "Y", I_BLUE: "B"}
# Corresponds to excluding that channel, 
# the combined color of the other 2 channels 
TAGS_2CHANNEL = {I_RED: "G", I_YELLOW: "P", I_BLUE: "O"}

def rgb_to_ryb(pixel):
    r, g, b = pixel
    return ryb_converter.rgb_to_ryb(r, g, b)

def get_mtrx_ryb(matrix): # Modifies parameter 
    for y, row in enumerate(matrix):
        for x, e in enumerate(row):
            matrix[y][x] = rgb_to_ryb(e)
    return matrix

def extract_channel(matrix, i_channel):
    return matrix[:, :, i_channel]

def dummy_other_channel(matrix, i_channel):
    for y, row in enumerate(matrix):
        for x, e in enumerate(row):
            e_new = list(rgb_to_ryb(e))
            if i_channel != I_YELLOW:
                for i in range(NUM_CHANNEL):
                    if i != i_channel:
                        e_new[i] = 0 # Make other channel to dummy
            else:
                # To show yellow in rgb, set blue channel to 0, 
                # red and green channel to magintude of yellow
                e_new[I_RED] = e_new[I_YELLOW]
                e_new[I_BLUE] = 0
            matrix[y][x] = e_new
    return matrix

def get_channels_except(matrix, i_channel, color_system):
    for y, row in enumerate(matrix):
        for x, e in enumerate(row):
            e_new = list(rgb_to_ryb(e))
            if color_system == "RYB":
                if i_channel == I_BLUE: # Mix red and yellow to get orange
                    e_new[I_RED] = get_channel_sum(e_new[I_RED], e_new[I_YELLOW])
                    e_new[I_BLUE] = 0;
                elif i_channel == I_YELLOW: # Mix red and blue to get purple
                    e_new[I_RED] = get_channel_sum(e_new[I_RED], e_new[I_RED] * 0.1)
                    e_new[I_YELLOW] = 0
                    e_new[I_BLUE] *= 0.9
                else: # I_Red; Mix yellow and blue to get green 
                    e_new[I_YELLOW] = get_channel_sum(e_new[I_YELLOW], matrix[y][x][I_GREEN] * 0.4)
                    e_new[I_RED] = e_new[I_YELLOW] * 0.7
            else: # Using RGB color space 
                if i_channel == I_BLUE:
                    e_new[I_GREEN] *= 0.647
                    e_new[I_BLUE] = 0
                elif i_channel == I_YELLOW:
                    e_new[I_RED] = 0.502
                    e_new[I_GREEN] = 0
                    e_new[I_BLUE] = 0.502
                else: # I_RED
                    e_new[I_RED] = 0
                    e_new[I_BLUE] = 0
            matrix[y][x] = e_new
    return matrix

def get_channel_sum(mag1, mag2):
    s = int(mag1) + int(mag2)
    return s if s <= 255 else 255

def write_channel(matrix, i_channel, name_img, path="", tag=""):
    tag = TAGS_CHANNEL[i_channel] + (("_" + tag) if tag!="" else "")
    image_factory.write_image_by_matrix(matrix, name_img, path, tag)

def write_2channel(matrix, i_channel, name_img, path="", tag=""):
    tag = TAGS_2CHANNEL[i_channel] + (("_" + tag) if tag!="" else "")
    image_factory.write_image_by_matrix(matrix, name_img, path, tag)

def filter_img_by_channel(name_img, show_in_color=True):
    mtrx = image_factory.get_matrix_from_uri(name_img)
    mtrx = get_mtrx_ryb(mtrx)
    if show_in_color:
        for i_channel in range(NUM_CHANNEL):
            mtrx_d = mtrx.copy()
            mtrx_d = dummy_other_channel(mtrx_d, i_channel)
            write_channel(mtrx_d, i_channel, name_img[:-4])
    else:
        for i_channel in range(NUM_CHANNEL):
            mtrx_e = mtrx.copy()
            mtrx_e = extract_channel(mtrx_e, i_channel)
            write_channel(mtrx_e, i_channel, name_img[:-4], tag="L")

def filter_img_by_2channel(name_img, color_system = "RYB"):
    mtrx = image_factory.get_matrix_from_uri(name_img)
    if (color_system == "RYB"): #Using RYB color space
        mtrx = get_mtrx_ryb(mtrx)
    # for i_channel in range(NUM_CHANNEL):
    #     mtrx_e = mtrx.copy()
    #     mtrx_e = get_channels_except(mtrx, i_channel, color_system)
    #     write_2channel(mtrx_e, i_channel, name_img[:-4])
    i_channel = I_YELLOW
    mtrx_e = mtrx.copy()
    mtrx_e = get_channels_except(mtrx, i_channel, color_system)
    write_2channel(mtrx_e, i_channel, name_img[:-4])

if __name__ == "__main__":
    names_img = ["Forest.jpg", "Reflection.jpg", "Still Life.jpg"]
    # for i in range(len(names_img)):
    #     filter_img_by_channel(names_img[i], True)
    name_img = "Still Life.jpg"
    for i in range(len(names_img)):
        filter_img_by_2channel(names_img[i], "RYB")
