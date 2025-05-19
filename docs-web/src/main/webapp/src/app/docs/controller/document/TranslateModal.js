'use strict';

angular.module('docs').controller('TranslateModal', function($scope, $uibModalInstance, file, Restangular, $rootScope) {
  // 支持的语言列表，可根据实际需求扩展
  $scope.languages = [
    { code: 'auto', name: '自动识别' },
    { code: 'zh', name: '中文' },
    { code: 'en', name: '英语' },
    { code: 'ja', name: '日语' },
    { code: 'ko', name: '韩语' },
    { code: 'fr', name: '法语' },
    { code: 'de', name: '德语' }
    // ...更多
  ];

  $scope.selectedSourceLang = 'auto';
  $scope.selectedTargetLang = 'zh';
  $scope.translatedText = null;
  $scope.translating = false;
  $scope.error = null;

  $scope.translate = function() {
    $scope.translating = true;
    $scope.error = null;
    $scope.translatedText = null;

    // 调用后端API
    Restangular.one('document', file.documentId || file.id)
      .all('translate/auto')
      .post({
        lang: $scope.selectedTargetLang,
        userId: $rootScope.userInfo ? $rootScope.userInfo.id : null
      })
      .then(function(resp) {
        $scope.translatedText = resp.translatedText;
        $scope.translating = false;
      }, function(err) {
        $scope.error = '翻译失败: ' + (err.data && err.data.message ? err.data.message : '未知错误');
        $scope.translating = false;
      });
  };

  $scope.close = function() {
    $uibModalInstance.close();
  };
});