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
    // 检查文件对象是否有效
    if (!file || !file.id) {
      console.error('Invalid file object in translate function:', file);
      $scope.error = '无效的文件对象';
      return;
    }

    $scope.translating = true;
    $scope.error = null;
    $scope.translatedText = null;

    console.log('Translating file:', file);
    console.log('File ID:', file.id);
    console.log('Document ID:', $stateParams.id);

    // 构建请求数据
    var requestData = {
      fileId: file.id,
      lang: $scope.selectedTargetLang,
      userId: $rootScope.userInfo ? $rootScope.userInfo.id : null,
      share: $stateParams.share
    };
    console.log('Request data:', requestData);

    // 调用后端API，添加 fileId 参数
    Restangular.one('document', $stateParams.id)
      .all('translate/auto')
      .post(requestData)
      .then(function(resp) {
        console.log('Translation response:', resp);
        $scope.translatedText = resp.translatedText;
        $scope.translating = false;
      }, function(err) {
        console.error('Translation error:', err);
        $scope.error = '翻译失败: ' + (err.data && err.data.message ? err.data.message : '未知错误');
        $scope.translating = false;
      });
  };

  $scope.close = function() {
    $uibModalInstance.close();
  };
});