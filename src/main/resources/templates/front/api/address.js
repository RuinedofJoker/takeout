//f
//获取所有地址
function addressListApi() {
    return $axios({
      'url': '/addressBook/list',
      'method': 'get',
    })
  }

//获取最新地址
function addressLastUpdateApi() {
    return $axios({
      'url': '/addressBook/lastUpdate',
      'method': 'get',
    })
}

//f
//新增地址
function  addAddressApi(data){
    return $axios({
        'url': '/addressBook',
        'method': 'post',
        data
      })
}

//f
//修改地址
function  updateAddressApi(data){
    return $axios({
        'url': '/addressBook',
        'method': 'put',
        data
      })
}

//f
//删除地址
function deleteAddressApi(params) {
    return $axios({
        'url': '/addressBook',
        'method': 'delete',
        params
    })
}

//f
//查询单个地址
function addressFindOneApi(id) {
  return $axios({
    'url': `/addressBook/${id}`,
    'method': 'get',
  })
}

//f
//设置默认地址
function  setDefaultAddressApi(data){
  return $axios({
      'url': '/addressBook/default',
      'method': 'put',
      data
    })
}

//f
//获取默认地址
function getDefaultAddressApi() {
  return $axios({
    'url': `/addressBook/default`,
    'method': 'get',
  })
}
