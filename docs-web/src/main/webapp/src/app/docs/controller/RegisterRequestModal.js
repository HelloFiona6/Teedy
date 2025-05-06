angular.module('docs').controller('RegisterRequestModal', function($scope, $uibModalInstance, User, Restangular) {
  $scope.register = {};
  $scope.registerStatus = null;

  $scope.submitRegister = function() {
    Restangular.one('user/register_request').customPOST($scope.register).then(function(resp) {
      $scope.registerStatus = "注册请求已提交，请等待管理员审核。您的请求ID：" + resp.id;
    }, function(err) {
      $scope.registerStatus = (err.data && err.data.message) ? err.data.message : "提交失败";
    });
  };
  $scope.openCheckStatus = function() {
    $uibModal.open({
      templateUrl: 'partial/docs/registerrequeststatus.html',
      controller: 'RegisterRequestStatusModal'
    });
  };
});
