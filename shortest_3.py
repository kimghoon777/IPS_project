import copy

def short(departure,destination):
    print("-----------[", departure, "->", destination,"]----------")
    landscape = {
    '신발류': {'과일류':537, '육류':9999,'화장품':9999,'음료':9999,'문구류':9999,'채소류':9999,'과자류':931,'A':586,'B':9999},
    '과일류': {'신발류':537,  '육류':9999,'화장품':9999,'음료':9999,'문구류':9999,'채소류':9999,'과자류':985,'A':159,'B':9999},
    '육류': {'신발류':9999, '과일류':9999,'화장품':862,'음료':9999,'문구류':9999,'채소류':492,'과자류':9999,'A':426,'B':1719},
    '화장품': {'신발류':9999, '과일류':9999, '육류':862,'음료':9999,'문구류':9999,'채소류':472,'과자류':9999,'A':1269,'B':861},
    '음료': {'신발류':9999, '과일류':9999, '육류':9999,'화장품':9999,'문구류':499,'채소류':9999,'과자류':9999,'A':9999,'B':610},
    '문구류': {'신발류':9999, '과일류':9999, '육류':9999,'화장품':9999,'음료':499,'채소류':9999,'과자류':9999,'A':9999,'B':469},
    '채소류': {'신발류':9999, '과일류':9999, '육류':492,'화장품':472,'음료':9999,'문구류':9999,'과자류':9999,'A':854,'B':1277},
    '과자류': {'신발류':931, '과일류':985, '육류':9999,'화장품':9999,'음료':9999,'문구류':9999,'채소류':9999,'A':859,'B':9999},
    'A': {'신발류':586, '과일류':159, '육류':423,'화장품':1269,'음료':9999,'문구류':9999,'채소류':854,'과자류':859,'B':2123},
    'B': {'신발류':9999, '과일류':9999, '육류':1719,'화장품':861,'음료':610,'문구류':469,'채소류':1277,'과자류':9999,'A':2123},
    }
    routing = {}
    for place in landscape.keys():
        routing[place]={'shortestDist':0, 'route':[], 'visited':0}
    def visitPlace(visit):
        routing[visit]['visited'] = 1
        for toGo, betweenDist in landscape[visit].items():
            toDist = routing[visit]['shortestDist'] + betweenDist
            if (routing[toGo]['shortestDist'] >= toDist) or not routing[toGo]['route']:
                routing[toGo]['shortestDist'] = toDist
                routing[toGo]['route'] = copy.deepcopy(routing[visit]['route'])
                routing[toGo]['route'].append(visit)
    visitPlace(departure[2:])
    while 1 :
        minDist = max(routing.values(), key=lambda x:x['shortestDist'])['shortestDist']
        toVisit = ''
        for name, search in routing.items():
            if 0 < search['shortestDist'] <= minDist and not search['visited']:
                minDist = search['shortestDist']
                toVisit = name
        if toVisit == '':
            break
        visitPlace(toVisit)
        print("["+toVisit+"]")
        print("Dist :", minDist)

    print("\n", "[", departure, "->", destination,"]")
    print("Route : ", routing[destination]['route'],destination)
    print("ShortestDistance : ", routing[destination]['shortestDist'])

    route = routing[destination]['route']
    route2 = ':'.join(route)
    print(route2)
    print("확인: " + str(len(routing[destination]['route'])))


    all_route=""
    ####이거 0,1,2
    for i in range(1,len(routing[destination]['route'])+1):
        all_route = all_route + ","+routing[destination]['route'][i-1]
    all_route=all_route+","+destination

    all_route_2 = all_route[1:]
    print("확인: " + all_route_2)

    return all_route_2
# 육류,B,음료
#short("육류","음료")