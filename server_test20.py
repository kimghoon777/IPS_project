from socket import *
import location
import calculateDistance
import trackPhone
import shortest_3
import route_position

# 5900번은 라즈베리파이 vnc용
# 5800번은 소켓통신용

# 소켓 통신
serverSock = socket(AF_INET, SOCK_STREAM)
serverSock.bind(('121.155.161.100', 5800))
serverSock.listen(1)

flag = 0
i=0
temp = []
rssi_box=[]
beacon_box=[]
num=0
plz = []
rssi_1 = 0
rssi_2 = 1
rssi_3 = 2

while True:
    connectionSock, addr = serverSock.accept()
    data = connectionSock.recv(1024).decode('utf-8')

    # ~에서 접속이 확인되었습니다 1번만 출력
    if flag is 0:
        print(str(addr), '에서 접속이 확인되었습니다.')
        flag = 1

    # if test=="-" : rssi 값을 받으면 실행
    if test=="-":
        box1 = data[2:].split(" ") # 받은 데이터를 공백으로 분리/저장
        print(" ######################## :  "+str(box1))

        while i != 13:
            temp.append(int(box1[i])) # rssi값 리스트 저장
            plz.append(int(box1[i])) # rssi값 순위를 정하기 위한 리스트 저장
            i = i + 1

        i = 0
        j = 1
        print("temp 크기 확인: " + str(len(temp)))


        if(len(temp)==13):
            # RSSI값 줄세우기
            while i != 12: # temp[0] ~ temp[12]
                while j != 13: # temp[1] ~ tmep[13]
                    if (temp[i] < temp[j]):
                        zz = temp[i]
                        temp[i] = temp[j]
                        temp[j] = zz
                    j = j + 1
                i = i + 1
                j = i + 1
            print("temp1 길이 확인 : " + str(temp))
        else: # 비콘의 세기값이 11개만 수신되어 저장되는 알 수 없는 오류 발생.
            print("temp2 길이 확인 : " + str(temp))
            i=0
            j=1
            # 리스트의 길이를 13으로 맞추기위해 append
            temp.append(-100)
            temp.append(-100)
            plz.append(-100)
            plz.append(-100)
            print("temp 길이 확인 : "+str(len(temp)))

            while i != 12:  # temp[0] ~ temp[12]
                while j != 13:  # temp[1] ~ tmep[13]
                    if (temp[i] < temp[j]):
                        zz = temp[i]
                        temp[i] = temp[j]
                        temp[j] = zz
                    j = j + 1
                i = i + 1
                j = i + 1

        print("plz 리스트 확인 : " + str(plz))

        # 가장 세기값이 큰 3개의 값 저장
        for a in range(0,13):
            if(temp[0]==plz[a]):
                rssi_1=a+1
            elif(temp[1]==plz[a]):
                rssi_2=a+1
            elif(temp[2]==plz[a]):
                rssi_3=a+1
            else:
                print("?")


        # 1번: 가장 세기가 큰 비콘 번호
        # 2번: 두 번째로 세기가 큰 비콘 번호
        # 3번: 세 번째로 세기가 큰 비콘 번호
        print("1번: " +str(rssi_1)+
              "\n2번: " +str(rssi_2)+
              "\n3번: " +str(rssi_3))
        i = 0
        j = 0

        while i != 13:
            print(i, "확인3: " + str(temp[i]))
            i = i + 1

        i = 0

        # 크기가 가장 큰 세개의 비콘의 좌표값 저장
        list_loc_1 = list(location.loc(rssi_1)) # 1210,470
        list_loc_2 = list(location.loc(rssi_2))
        list_loc_3 = list(location.loc(rssi_3))

        try: # 사용자 좌표 구하기
            result = trackPhone.tP(list_loc_1[0],list_loc_1[1],calculateDistance.cD(-59,temp[0]),
                                list_loc_2[0],list_loc_2[1],calculateDistance.cD(-59,temp[1]),
                                list_loc_3[0],list_loc_3[1],calculateDistance.cD(-59,temp[2]))
            res=str(result[0])+","+str(result[1])
            print(res)
            # 전송
            connectionSock.send(res.encode('utf-8'))
        except ValueError:
            print("Error")

        # 변수 초기화
        del temp[:]
        a=0
        del plz[:]

    # if test=="-" 의 else : 경로 값을 받으면 실행
    else:
        box2 = data.split(",")
        # 받아온 구매리스트 정보 확인
        print(box2[0] + " : "+box2[1])

        # route 정보 리스트
        route_inf=[]
        route_inf=shortest_3.short(box2[0],box2[1]) # 육류,B,음료
        route_inf_list = route_inf.split(",") # list[0]=육류, 1-B 2-음료
        route_inf_list_size = len(route_inf_list)  # 3
        arrow=[] # 경로표시 리스트 / 화살표
        i=0
        # 경로의 사이즈만큼 반복해서 표시할 경로정보를 앱으로 전송
        for i in range(0,route_inf_list_size-1): # 0,1 두 번
            print("보냄1번: " + str(i))
            arrow.append(","+route_position.rp(route_inf_list[i],route_inf_list[i+1])) # 육류-B // B-음료
            print("보냄2번: " + str(i))
            connectionSock.send(arrow[i].encode('utf-8'))
            print("보냄: "+str(i) +": "+arrow[i])
        # 변수 초기환
        del arrow[:]