# 비콘좌표
import random

beacon_1_x = 1210
beacon_1_y = 470

beacon_2_x = 950
beacon_2_y = 1900

beacon_3_x = 800
beacon_3_y = 2000

beacon_4_x = 918
beacon_4_y = 298

beacon_5_x = 420
beacon_5_y = 700

beacon_6_x = 1100
beacon_6_y = 615


beacon_7_x = 880
beacon_7_y = 750

beacon_8_x = 1100
beacon_8_y = 1340


beacon_9_x = 500
beacon_9_y = 250

beacon_10_x = 1120
beacon_10_y = 1650

beacon_11_x = 1120
beacon_11_y = 955

beacon_12_x = 330
beacon_12_y = 1950

beacon_13_x = 580
beacon_13_y = 2210

def loc(beacon):
    if beacon == 1:
        return beacon_1_x, beacon_1_y
    elif beacon == 2:
        return beacon_2_x, beacon_2_y
    elif beacon == 3:
        return beacon_3_x, beacon_3_y
    elif beacon == 4:
        return beacon_4_x, beacon_4_y
    elif beacon == 5:
        return beacon_5_x, beacon_5_y
    elif beacon == 6:
        return beacon_6_x, beacon_6_y
    elif beacon == 7:
        return beacon_7_x, beacon_7_y
    elif beacon == 8:
        return beacon_8_x, beacon_8_y
    elif beacon == 9:
        return beacon_9_x, beacon_9_y
    elif beacon == 10:
        return beacon_10_x, beacon_10_y
    elif beacon == 11:
        return beacon_11_x, beacon_11_y
    elif beacon == 12:
        return beacon_12_x, beacon_12_y
    elif beacon == 13:
        return beacon_13_x, beacon_13_y
    else: # 수신 못했을 시 랜덤값 return -> 오류 방지
        print("random")
        return random.randint(250,500),random.randint(300,800)
