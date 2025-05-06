angular.module('docs').controller('RegisterRequestStatusModal', function($scope, $uibModalInstance, Restangular) {
  $scope.requestId = '';
  $scope.statusMsg = null;

  $scope.checkStatus = function() {
    if (!$scope.requestId) return;
    Restangular.one('user/register_request', $scope.requestId).one('status').get().then(function(resp) {
      $scope.statusMsg = "当前状态：" + resp.status;
    }, function(err) {
      $scope.statusMsg = (err.data && err.data.message) ? err.data.message : "查询失败";
    });
  };
});
