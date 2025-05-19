// angular.module('docs').controller('RegisterRequestAdmin', function($scope, Restangular) {
//   $scope.requests = [];

//   function loadRequests() {
//     Restangular.one('user/register_request/list').get().then(function(data) {
//       $scope.requests = data;
//     });
//   }

//   $scope.accept = function(req) {
//     Restangular.one('user/register_request', req.id).customPOST({}, 'accept').then(loadRequests);
//   };

//   $scope.reject = function(req) {
//     var comment = prompt("请输入拒绝理由：");
//     Restangular.one('user/register_request', req.id).customPOST({comment: comment}, 'reject').then(loadRequests);
//   };

//   loadRequests();
// });
