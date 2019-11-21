def rp(A,B):
    if A=="육류":  # 4개 끝 #################
        if B=="B":
            return "line_11_b"
        elif B=="A":
            return "line_a_11"
        elif B=="채소류":
            return "line_8_11"
        elif B=="화장품":
            return "line_11_10"

    elif A=="B":  # 6개 끝 #################
        if B=="음료":
            return "line_13_b"
        elif B=="육류":
            return "line_11_b"
        elif B=="A":
            return "line_a_b"
        elif B=="문구류":
            return "line_3_b"
        elif B=="화장품":
            return "line_b_10"
        elif B=="채소류":
            return "line_8_b"

    elif A=="음료": # 2개 끝 #################
        if B=="B":
            return "line_13_b"
        elif B=="문구류":
            return "line_13_3"

    elif A=="과자류": # 3개 #################
        if B=="A":
            return "line_5_a"
        elif B=="신발류":
            return "line_5_4"
        elif B=="과일류":
            return "line_5_6"

    elif A=="A": # 7개 끝 ##############
        if B=="B":
            return "line_a_b"
        elif B=="과자류":
            return "line_5_a"
        elif B=="육류":
            return "line_a_11"
        elif B == "신발류":
            return "line_4_a"
        elif B == "과일류":
            return "line_a_6"
        elif B == "채소류":
            return "line_a_8"
        elif B == "화장품":
            return "line_a_10"

    elif A=="문구류": # 2개 끝 ##############
        if B=="B":
            return "line_3_b"
        elif B=="음료":
            return "line_13_3"

    elif A=="채소류": # 4개 끝 ##############
        if B=="육류":
            return "line_8_11"
        elif B=="B":
            return "line_8_b"
        elif B=="A":
            return "line_a_8"
        elif B=="화장품":
            return "line_8_10"

    elif A=="화장품": # 4개 ##############
        if B=="륙류":
            return "line_11_10"
        elif B=="B":
            return "line_b_10"
        elif B=="채소류":
            return "line_8_10"
        elif B=="A":
            return "line_a_10"

    elif A=="신발류": # 3개
        if B=="과자류":
            return "line_5_4"
        elif B=="신발류":
            return "line_4_a"
        elif B=="과일류":
            return "line_a_6"

    elif A=="과일류":
        if B=="신발류":
            return "line_4_6"
        elif B=="A":
            return "line_a_6"
        elif B=="과자":
            return "line_5_6"