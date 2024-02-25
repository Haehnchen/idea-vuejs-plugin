<template>
    <div>
      <dynamic-renderer></dynamic-renderer>
      <static-renderer></static-renderer>
    </div>
</template>

<script>
import dynamicRenderer from '~/components/dynamic-renderer';
import staticRenderer from '~/components/static-renderer';

export default {
  components: {
    'dynamic-renderer': dynamicRenderer,
    'static-renderer': staticRenderer
  }
}
</script>
