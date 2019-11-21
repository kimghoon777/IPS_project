# 거리 계산
def cD(txPower, rssi):
  if (rssi == 0):
    return -1.0 # if we cannot determine distance, return -1.
  ratio = rssi*1.0/txPower
  if (ratio < 1.0):
    return pow(ratio,10)
  else:
    accuracy =  (0.89976)*pow(ratio,7.7095) + 0.111
    return accuracy