'use strict';

angular.module('docs').controller('TranslateModal', function($scope, $uibModalInstance, file, Restangular, $rootScope, $stateParams) {
  console.log('TranslateModal initialized with file:', file);
  console.log('Document ID:', $stateParams.id);

  // 检查文件对象是否有效
  if (!file || !file.id) {
    console.error('Invalid file object:', file);
    $scope.error = '无效的文件对象';
    return;
  }

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

  $scope.file = file;
  $scope.selectedSourceLang = 'auto';
  $scope.selectedTargetLang = 'zh';
  $scope.translatedText = null;
  $scope.translating = false;
  $scope.error = null;

  $scope.translate = function() {
    if (!$scope.file || !$scope.file.id) {
      $scope.error = "请选择要翻译的文件";
      return;
    }
    if (!$scope.selectedTargetLang) {
      $scope.error = "请选择目标语言";
      return;
    }

    $scope.translating = true;
    $scope.error = null;
    $scope.translatedText = null;

    Restangular.one('document/autoTranslate').post('', {
      fileId: $scope.file.id,
      targetLang: $scope.selectedTargetLang
    }).then(function(response) {
      $scope.translating = false;
      $scope.translatedText = response.translatedText;
    }, function(error) {
      $scope.translating = false;
      $scope.error = error.data.message || "翻译失败";
    });
  };

  $scope.close = function() {
    $uibModalInstance.close();
  };
});